package com.preethamm.nova

import com.preethamm.nova.data.repository.InMemoryConversationRepository
import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.MessageRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryConversationRepositoryTest {
    @Test
    fun add_appendsMessagesInOrder() = runTest {
        val repository = InMemoryConversationRepository()

        repository.add(ConversationMessage(id = 1L, role = MessageRole.USER, content = "Hello"))
        repository.add(ConversationMessage(id = 2L, role = MessageRole.NOVA, content = "Hi"))

        assertEquals(listOf("Hello", "Hi"), repository.messages.value.map { it.content })
    }

    @Test
    fun clear_removesMessages() = runTest {
        val repository = InMemoryConversationRepository()

        repository.add(ConversationMessage(id = 1L, role = MessageRole.USER, content = "Hello"))
        repository.clear()

        assertTrue(repository.messages.value.isEmpty())
    }
}
