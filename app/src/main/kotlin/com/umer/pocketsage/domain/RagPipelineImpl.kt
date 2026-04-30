package com.umer.pocketsage.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagPipelineImpl @Inject constructor(
    private val embed: EmbeddingService,
    private val retriever: Retriever,
    private val llm: LlmRunner,
) : RagPipeline {

    override fun answer(question: String, scope: ChatScope): Flow<RagEvent> = flow {
        try {
            emit(RagEvent.Retrieving)

            val qVec = embed.embed(question)
            val docFilter = when (scope) {
                is ChatScope.AllDocuments -> null
                is ChatScope.OneDocument -> listOf(scope.id)
            }
            val hits = retriever.topK(qVec, docFilter, k = 4)
            emit(RagEvent.Retrieved(hits))

            val context = hits.joinToString("\n\n") { it.text }
            val prompt = buildPrompt(context, question)

            llm.generate(prompt).collect { token -> emit(RagEvent.Token(token)) }

            emit(RagEvent.Completed)
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            emit(RagEvent.Error(t))
        }
    }

    private fun buildPrompt(context: String, question: String): String =
        """
        You are a helpful assistant. Use ONLY the context below to answer the question.
        If the answer is not contained in the context, reply: "I don't know based on
        the provided documents."

        Context:
        ---
        $context
        ---

        Question: $question

        Answer:
        """.trimIndent()
}