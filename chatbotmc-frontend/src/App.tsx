import { useState, useRef, useEffect } from 'react'
import {
  Box,
  TextField,
  IconButton,
  Paper,
  Typography,
  Container,
  CircularProgress,
  ThemeProvider,
  createTheme,
  CssBaseline,
  Avatar,
  Button,
  Snackbar,
  Alert,
} from '@mui/material'
import SendIcon from '@mui/icons-material/Send'
import SmartToyIcon from '@mui/icons-material/SmartToy'
import PersonIcon from '@mui/icons-material/Person'
import LoginIcon from '@mui/icons-material/Login'
import PersonAddIcon from '@mui/icons-material/PersonAdd'
import LogoutIcon from '@mui/icons-material/Logout'
import LoginDialog from './components/LoginDialog'
import RegisterDialog from './components/RegisterDialog'
import ConversationSidebar from './components/ConversationSidebar'
import { logout, isAuthenticated } from './services/authService'
import { conversationService, type Conversation } from './services/conversationService'
import './App.css'

interface Message {
  id: number
  text: string
  sender: 'user' | 'bot'
  timestamp: Date
}

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#10a37f',
    },
    background: {
      default: '#343541',
      paper: '#444654',
    },
  },
  typography: {
    fontFamily: '"Segoe UI", "Helvetica Neue", Arial, sans-serif',
  },
})

function App() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      text: 'Hello! I\'m your AI assistant. How can I help you today?',
      sender: 'bot',
      timestamp: new Date(),
    },
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [loginOpen, setLoginOpen] = useState(false)
  const [registerOpen, setRegisterOpen] = useState(false)
  const [authenticated, setAuthenticated] = useState(isAuthenticated())
  const [username, setUsername] = useState(localStorage.getItem('username') || '')
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarMessage, setSnackbarMessage] = useState('')
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  // Load conversations when authenticated
  useEffect(() => {
    if (authenticated) {
      loadConversations()
    }
  }, [authenticated])

  const loadConversations = async () => {
    try {
      const convs = await conversationService.getAllConversations()
      setConversations(convs)
    } catch (error) {
      console.error('Error loading conversations:', error)
    }
  }

  const loadConversationMessages = async (conversationId: number) => {
    try {
      const msgs = await conversationService.getConversationMessages(conversationId)
      const formattedMessages: Message[] = msgs.map((msg) => ({
        id: msg.id,
        text: msg.content,
        sender: msg.role === 'USER' ? 'user' : 'bot',
        timestamp: new Date(msg.createdAt),
      }))
      setMessages(formattedMessages)
      setCurrentConversationId(conversationId)
    } catch (error) {
      console.error('Error loading messages:', error)
      setSnackbarMessage('Failed to load conversation')
      setSnackbarOpen(true)
    }
  }

  const handleNewConversation = () => {
    setCurrentConversationId(null)
    setMessages([
      {
        id: 1,
        text: 'Hello! I\'m your AI assistant. How can I help you today?',
        sender: 'bot',
        timestamp: new Date(),
      },
    ])
  }

  const handleDeleteConversation = async (conversationId: number) => {
    try {
      await conversationService.deleteConversation(conversationId)
      setConversations(conversations.filter((c) => c.id !== conversationId))
      if (currentConversationId === conversationId) {
        handleNewConversation()
      }
      setSnackbarMessage('Conversation deleted')
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error deleting conversation:', error)
      setSnackbarMessage('Failed to delete conversation')
      setSnackbarOpen(true)
    }
  }

  const handleRenameConversation = async (conversationId: number, newTitle: string) => {
    try {
      await conversationService.updateConversationTitle(conversationId, newTitle)
      setConversations(
        conversations.map((c) => (c.id === conversationId ? { ...c, title: newTitle } : c))
      )
      setSnackbarMessage('Conversation renamed')
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error renaming conversation:', error)
      setSnackbarMessage('Failed to rename conversation')
      setSnackbarOpen(true)
    }
  }

  const sendMessage = async () => {
    if (!input.trim() || loading) return

    // Check if user is authenticated
    if (!authenticated) {
      setSnackbarMessage('Please login to use the chatbot')
      setSnackbarOpen(true)
      setLoginOpen(true)
      return
    }

    const userMessage: Message = {
      id: Date.now(),
      text: input,
      sender: 'user',
      timestamp: new Date(),
    }

    setMessages((prev) => [...prev, userMessage])
    const currentInput = input
    setInput('')
    setLoading(true)

    try {
      const token = localStorage.getItem('token')
      const response = await fetch('http://localhost:8080/api/llm/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ 
          prompt: currentInput,
          conversationId: currentConversationId 
        }),
      })

      if (response.status === 401 || response.status === 403) {
        // Token expired or invalid
        logout()
        setAuthenticated(false)
        setUsername('')
        setSnackbarMessage('Session expired. Please login again.')
        setSnackbarOpen(true)
        setLoginOpen(true)
        return
      }

      if (!response.ok) {
        throw new Error('Failed to get response')
      }

      const data = await response.json()

      const botMessage: Message = {
        id: data.messageId || Date.now() + 1,
        text: data.response,
        sender: 'bot',
        timestamp: new Date(),
      }

      setMessages((prev) => [...prev, botMessage])
      
      // Update conversation ID if this was a new conversation
      if (!currentConversationId && data.conversationId) {
        setCurrentConversationId(data.conversationId)
        // Reload conversations to show the new one in the sidebar
        loadConversations()
      }
    } catch (error) {
      console.error('Error:', error)
      const errorMessage: Message = {
        id: Date.now() + 1,
        text: 'Sorry, I encountered an error. Please make sure the backend server is running.',
        sender: 'bot',
        timestamp: new Date(),
      }
      setMessages((prev) => [...prev, errorMessage])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const handleLoginSuccess = (user: string) => {
    setAuthenticated(true)
    setUsername(user)
    setSnackbarMessage(`Welcome back, ${user}!`)
    setSnackbarOpen(true)
  }

  const handleRegisterSuccess = (user: string) => {
    // Don't authenticate yet - user needs admin approval first
    setSnackbarMessage(`Registration successful, ${user}! Your account is pending admin approval. You'll receive an email once approved.`)
    setSnackbarOpen(true)
  }

  const handleLogout = () => {
    logout()
    setAuthenticated(false)
    setUsername('')
    setConversations([])
    setCurrentConversationId(null)
    setMessages([
      {
        id: 1,
        text: 'Hello! I\'m your AI assistant. How can I help you today?',
        sender: 'bot',
        timestamp: new Date(),
      },
    ])
    setSnackbarMessage('Logged out successfully')
    setSnackbarOpen(true)
  }

  const handleSwitchToRegister = () => {
    setLoginOpen(false)
    setRegisterOpen(true)
  }

  const handleSwitchToLogin = () => {
    setRegisterOpen(false)
    setLoginOpen(true)
  }

  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <Box
        sx={{
          height: '100vh',
          display: 'flex',
          bgcolor: 'background.default',
        }}
      >
        {/* Sidebar - only show when authenticated */}
        {authenticated && (
          <ConversationSidebar
            conversations={conversations}
            currentConversationId={currentConversationId}
            onSelectConversation={(id) => {
              if (id !== null) {
                loadConversationMessages(id)
              }
            }}
            onDeleteConversation={handleDeleteConversation}
            onRenameConversation={handleRenameConversation}
            onNewConversation={handleNewConversation}
          />
        )}

        {/* Main Chat Area */}
        <Box
          sx={{
            flexGrow: 1,
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            bgcolor: 'background.default',
          }}
        >
        {/* Header */}
        <Paper
          elevation={2}
          sx={{
            p: 2,
            borderRadius: 0,
            bgcolor: 'background.paper',
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
          }}
        >
          <Container maxWidth="md">
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Typography variant="h5" fontWeight="600" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SmartToyIcon /> ChatBot MC
              </Typography>
              
              {/* Login & Register Buttons OR User Info */}
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                {authenticated ? (
                  <>
                    <Typography variant="body2" sx={{ color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <PersonIcon fontSize="small" />
                      {username}
                    </Typography>
                    <Button
                      variant="outlined"
                      startIcon={<LogoutIcon />}
                      onClick={handleLogout}
                      sx={{
                        color: 'primary.main',
                        borderColor: 'primary.main',
                        '&:hover': {
                          borderColor: 'primary.light',
                          bgcolor: 'rgba(16, 163, 127, 0.08)',
                        },
                      }}
                    >
                      Logout
                    </Button>
                  </>
                ) : (
                  <>
                    <Button
                      variant="outlined"
                      startIcon={<LoginIcon />}
                      onClick={() => setLoginOpen(true)}
                      sx={{
                        color: 'primary.main',
                        borderColor: 'primary.main',
                        '&:hover': {
                          borderColor: 'primary.light',
                          bgcolor: 'rgba(16, 163, 127, 0.08)',
                        },
                      }}
                    >
                      Login
                    </Button>
                    <Button
                      variant="contained"
                      startIcon={<PersonAddIcon />}
                      onClick={() => setRegisterOpen(true)}
                      sx={{
                        bgcolor: 'primary.main',
                        '&:hover': {
                          bgcolor: 'primary.dark',
                        },
                      }}
                    >
                      Register
                    </Button>
                  </>
                )}
              </Box>
            </Box>
          </Container>
        </Paper>

        {/* Messages Area */}
        <Box
          sx={{
            flexGrow: 1,
            overflow: 'auto',
            py: 2,
          }}
        >
          <Container maxWidth="md">
            {messages.map((message) => (
              <Box
                key={message.id}
                sx={{
                  display: 'flex',
                  gap: 2,
                  mb: 3,
                  alignItems: 'flex-start',
                }}
              >
                <Avatar
                  sx={{
                    bgcolor: message.sender === 'user' ? 'primary.main' : 'grey.700',
                    width: 36,
                    height: 36,
                  }}
                >
                  {message.sender === 'user' ? <PersonIcon /> : <SmartToyIcon />}
                </Avatar>
                <Box sx={{ flex: 1 }}>
                  <Typography
                    variant="subtitle2"
                    sx={{ mb: 0.5, color: 'grey.400', fontWeight: 600 }}
                  >
                    {message.sender === 'user' ? 'You' : 'ChatBot'}
                  </Typography>
                  <Typography
                    variant="body1"
                    sx={{
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      lineHeight: 1.7,
                    }}
                  >
                    {message.text}
                  </Typography>
                </Box>
              </Box>
            ))}
            {loading && (
              <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'flex-start' }}>
                <Avatar sx={{ bgcolor: 'grey.700', width: 36, height: 36 }}>
                  <SmartToyIcon />
                </Avatar>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="subtitle2" sx={{ mb: 0.5, color: 'grey.400', fontWeight: 600 }}>
                    ChatBot
                  </Typography>
                  <CircularProgress size={20} />
                </Box>
              </Box>
            )}
            <div ref={messagesEndRef} />
          </Container>
        </Box>

        {/* Input Area */}
        <Paper
          elevation={3}
          sx={{
            p: 2,
            borderRadius: 0,
            bgcolor: 'background.paper',
            borderTop: '1px solid rgba(255, 255, 255, 0.1)',
          }}
        >
          <Container maxWidth="md">
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
              <TextField
                fullWidth
                multiline
                maxRows={4}
                placeholder={authenticated ? "Type your message here..." : "Please login to start chatting..."}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyPress={handleKeyPress}
                disabled={loading || !authenticated}
                variant="outlined"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    bgcolor: 'background.default',
                    borderRadius: 2,
                  },
                }}
              />
              <IconButton
                color="primary"
                onClick={sendMessage}
                disabled={!input.trim() || loading || !authenticated}
                sx={{
                  bgcolor: 'primary.main',
                  color: 'white',
                  '&:hover': {
                    bgcolor: 'primary.dark',
                  },
                  '&.Mui-disabled': {
                    bgcolor: 'grey.700',
                    color: 'grey.500',
                  },
                  width: 48,
                  height: 48,
                }}
              >
                <SendIcon />
              </IconButton>
            </Box>
            <Typography
              variant="caption"
              sx={{ mt: 1, display: 'block', textAlign: 'center', color: 'grey.500' }}
            >
              Press Enter to send, Shift+Enter for new line
            </Typography>
          </Container>
        </Paper>

        {/* Login Dialog */}
        <LoginDialog
          open={loginOpen}
          onClose={() => setLoginOpen(false)}
          onLoginSuccess={handleLoginSuccess}
          onSwitchToRegister={handleSwitchToRegister}
        />

        {/* Register Dialog */}
        <RegisterDialog
          open={registerOpen}
          onClose={() => setRegisterOpen(false)}
          onRegisterSuccess={handleRegisterSuccess}
          onSwitchToLogin={handleSwitchToLogin}
        />

        {/* Success Snackbar */}
        <Snackbar
          open={snackbarOpen}
          autoHideDuration={4000}
          onClose={() => setSnackbarOpen(false)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert onClose={() => setSnackbarOpen(false)} severity="success" sx={{ width: '100%' }}>
            {snackbarMessage}
          </Alert>
        </Snackbar>
        </Box>
        {/* End Main Chat Area */}
      </Box>
      {/* End Container */}
    </ThemeProvider>
  )
}

export default App
