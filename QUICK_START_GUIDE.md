# Quick Start Guide - Chat History Feature

## Prerequisites

Make sure you have:
- PostgreSQL running on localhost:5432
- Database `chatbot_db` created
- Environment variables set for:
  - `spring.datasource.password`
  - `gemini.api-key`

## Step 1: Start the Backend

```bash
cd chatbotmc
./mvnw spring-boot:run
# or if you're using Windows:
mvnw.cmd spring-boot:run
```

**What to expect:**
- Spring Boot will start on port 8080
- Hibernate will automatically create the new tables:
  - `conversations`
  - `chat_messages`
- You should see SQL logs in the console showing table creation

## Step 2: Start the Frontend

```bash
cd chatbotmc-frontend
npm install  # Only needed if you haven't installed dependencies yet
npm run dev
```

**What to expect:**
- Vite dev server will start (usually on http://localhost:5173)
- Browser will open automatically

## Step 3: Test the Feature

### A. Register/Login
1. Click "Register" button
2. Create a new account or login with existing credentials
3. You should see a sidebar appear on the left (initially empty)

### B. Start Your First Chat
1. Type a message in the input box
2. Press Enter or click Send
3. Wait for the AI response
4. **Check the sidebar** - A new conversation should appear with:
   - Title (first 50 chars of your message)
   - "Just now" timestamp
   - Message count

### C. Test Conversation Features

**Create Multiple Conversations:**
1. Click "New Chat" button (top of sidebar)
2. Send a different message
3. Another conversation appears in the sidebar

**Switch Between Conversations:**
1. Click on any conversation in the sidebar
2. All messages from that conversation load
3. Continue chatting in that conversation

**Rename a Conversation:**
1. Hover over a conversation in the sidebar
2. Click the Edit icon (pencil)
3. Enter a new title
4. Click Save

**Delete a Conversation:**
1. Hover over a conversation in the sidebar
2. Click the Delete icon (trash)
3. Confirm deletion
4. Conversation disappears from sidebar

### D. Verify Persistence
1. Send some messages
2. Refresh the browser page
3. Login again
4. **All conversations should still be there!**

## Troubleshooting

### Backend Issues

**Error: "Table 'conversations' doesn't exist"**
- Check that `spring.jpa.hibernate.ddl-auto=update` is in application.properties
- Restart the Spring Boot application
- Check console logs for table creation SQL

**Error: "JWT validation failed"**
- Make sure the JWT secret in application.properties is at least 256 bits
- Clear browser localStorage and login again

**Error: "User not found"**
- Make sure you're logged in
- Check that the JWT token contains userId claim

### Frontend Issues

**Sidebar not showing:**
- Make sure you're logged in
- Check browser console for errors
- Verify the token is stored: Open DevTools → Application → Local Storage

**Conversations not loading:**
- Check browser Network tab for failed requests
- Verify backend is running on port 8080
- Check backend console for errors

**"Session expired" message:**
- Your JWT token expired (24 hours by default)
- Just login again

### API Endpoints to Test Manually

You can test the API using curl or Postman:

```bash
# Get all conversations (replace TOKEN with your JWT)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/conversations

# Get messages in a conversation
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/conversations/1/messages

# Send a chat message
curl -X POST -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"prompt":"Hello","conversationId":null}' \
  http://localhost:8080/api/llm/chat
```

## Database Verification

Connect to your PostgreSQL database and run:

```sql
-- Check conversations table
SELECT * FROM conversations;

-- Check messages table
SELECT * FROM chat_messages;

-- Check a user's conversations
SELECT c.id, c.title, c.created_at, COUNT(m.id) as message_count
FROM conversations c
LEFT JOIN chat_messages m ON c.id = m.conversation_id
WHERE c.user_id = 1
GROUP BY c.id;
```

## Success Indicators

✅ Sidebar appears when logged in
✅ New conversations created automatically when sending first message
✅ Conversations appear in sidebar immediately after creation
✅ Clicking a conversation loads its messages
✅ Messages persist after page refresh
✅ Each user only sees their own conversations
✅ Edit and delete operations work correctly
✅ Timestamps show relative time ("5m ago", "2h ago")

## Next Steps

Once everything is working:
1. Test with multiple user accounts to verify user isolation
2. Try sending many messages to test performance
3. Test edge cases (empty messages, very long titles, etc.)
4. Consider adding the optional enhancements listed in CHAT_HISTORY_IMPLEMENTATION.md

## Getting Help

If you encounter issues:
1. Check the browser console for errors
2. Check the Spring Boot console for errors
3. Verify all files were created correctly
4. Make sure PostgreSQL is running
5. Verify JWT token is valid and contains userId
