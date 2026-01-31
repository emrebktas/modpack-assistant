# Chat History Implementation Summary

## What Was Implemented

A complete chat history system that allows users to:
- Create and manage multiple conversations
- View all their past conversations in a sidebar
- Load and continue previous conversations
- Rename conversations
- Delete conversations
- Automatically save all messages to the database

## Backend Changes

### 1. New Entities

#### `Conversation.java`
- Stores conversation metadata (title, timestamps, user association)
- One-to-many relationship with ChatMessage
- Many-to-one relationship with User

#### `ChatMessage.java`
- Stores individual messages
- Links to parent Conversation
- Has MessageRole (USER or ASSISTANT)

#### `MessageRole.java`
- Enum with USER and ASSISTANT values

### 2. New Repositories

- **ConversationRepository**: Find conversations by user, ordered by update time
- **ChatMessageRepository**: Find messages by conversation, ordered by creation time

### 3. New DTOs

- **ConversationDTO**: Lightweight conversation info for list view
- **ChatMessageDTO**: Message data for display
- **ChatRequest**: Updated to include optional conversationId
- **ChatResponse**: Returns response text, conversationId, and messageId

### 4. New Services

#### ConversationService
- `createConversation()`: Create new conversation
- `getUserConversations()`: Get all conversations for a user
- `getConversationMessages()`: Get all messages in a conversation
- `saveMessage()`: Save a message to a conversation
- `deleteConversation()`: Delete a conversation
- `updateConversationTitle()`: Rename a conversation
- `generateConversationTitle()`: Auto-generate title from first message

### 5. Updated Services

#### LlmService
- Added `chatWithHistory()` method that:
  - Creates new conversation if needed
  - Saves user message
  - Gets AI response
  - Saves AI message
  - Returns ChatResponse with conversation info

#### JwtService
- Added `extractUserId()` method to get userId from JWT token

### 6. New Controllers

#### ConversationController
- `GET /api/conversations`: List all user conversations
- `GET /api/conversations/{id}/messages`: Get messages in a conversation
- `POST /api/conversations`: Create new conversation
- `DELETE /api/conversations/{id}`: Delete conversation
- `PATCH /api/conversations/{id}/title`: Rename conversation

### 7. Updated Controllers

#### LlmController
- Updated `/api/llm/chat` endpoint to:
  - Extract userId from JWT
  - Accept optional conversationId
  - Return ChatResponse with conversation info

## Frontend Changes

### 1. New Service

#### conversationService.ts
- `getAllConversations()`: Fetch all conversations
- `getConversationMessages()`: Fetch messages for a conversation
- `createConversation()`: Create new conversation
- `deleteConversation()`: Delete conversation
- `updateConversationTitle()`: Rename conversation

### 2. New Component

#### ConversationSidebar.tsx
A sidebar component that displays:
- "New Chat" button
- List of all conversations with:
  - Title
  - Last updated time (formatted: "Just now", "5m ago", "2h ago", etc.)
  - Message count
  - Edit and delete buttons
- Edit dialog for renaming
- Delete confirmation dialog

### 3. Updated Component

#### App.tsx
Major updates:
- Added state for conversations and currentConversationId
- Load conversations on login
- Display ConversationSidebar when authenticated
- Updated sendMessage to include conversationId
- Handle conversation selection/loading
- Handle new conversation creation
- Handle conversation deletion and renaming
- Clear conversations on logout

## Database Schema

The application will automatically create these tables:

```sql
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    content TEXT NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

## How It Works

### User Flow:

1. **User logs in** → Conversations are loaded and displayed in sidebar
2. **User starts typing** → Creates a new conversation
3. **First message sent** → 
   - Conversation is created with auto-generated title
   - User message is saved
   - AI response is generated and saved
   - Conversation appears in sidebar
4. **User clicks on conversation** → Messages are loaded from database
5. **User continues conversation** → Messages are saved to same conversation
6. **User clicks "New Chat"** → Clears current conversation, ready for new one
7. **User edits title** → Updates conversation title in database
8. **User deletes conversation** → Removes conversation and all messages

### Technical Flow:

```
Frontend (App.tsx)
    ↓
conversationService.ts
    ↓
Spring Boot Controllers (ConversationController, LlmController)
    ↓
Services (ConversationService, LlmService)
    ↓
Repositories (ConversationRepository, ChatMessageRepository)
    ↓
Database (PostgreSQL)
```

## Features Included

✅ Persistent chat history per user
✅ Multiple conversations support
✅ Conversation management (create, read, update, delete)
✅ Auto-generated conversation titles
✅ Message timestamps
✅ User isolation (users can only see their own conversations)
✅ Responsive sidebar UI
✅ Real-time conversation list updates
✅ Error handling and user feedback

## Testing the Implementation

1. **Start the backend**: Make sure PostgreSQL is running and Spring Boot app starts
2. **Start the frontend**: `npm run dev` in chatbotmc-frontend folder
3. **Login**: Use the login dialog
4. **Start chatting**: Send a message - conversation is created automatically
5. **Check sidebar**: Your conversation should appear in the left sidebar
6. **Test features**:
   - Send more messages in the same conversation
   - Click "New Chat" to start a new conversation
   - Click on an old conversation to load its history
   - Rename a conversation using the edit button
   - Delete a conversation using the delete button

## Notes

- The first message of a conversation is used to auto-generate the title (first 50 characters)
- All conversations are user-specific (enforced by JWT authentication)
- Messages are ordered by creation time
- Conversations are ordered by last update time (most recent first)
- Deleting a conversation also deletes all its messages (cascade delete)
- The sidebar only shows when user is authenticated

## Future Enhancements (Optional)

Consider adding these features later:
- Search functionality to find specific conversations
- Export conversation to text/PDF
- Share conversations with other users
- Message editing/deletion
- Context window management (send only recent messages to LLM)
- Conversation folders/tags
- Pin important conversations
- Message reactions
- Markdown rendering for AI responses
- Code syntax highlighting in messages
