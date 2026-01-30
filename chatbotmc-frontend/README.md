# ChatBot MC Frontend

A modern, ChatGPT-style chat interface built with React, TypeScript, and Material-UI.

## Features

- 💬 Real-time chat interface
- 🎨 Modern dark theme UI (inspired by ChatGPT)
- ⚡ Fast and responsive
- 📱 Mobile-friendly design
- 🔄 Auto-scrolling to latest messages
- ⌨️ Keyboard shortcuts (Enter to send, Shift+Enter for new line)

## Tech Stack

- **React 19** - UI framework
- **TypeScript** - Type safety
- **Material-UI (MUI)** - Component library
- **Vite** - Build tool
- **Emotion** - CSS-in-JS styling

## Getting Started

### Prerequisites

- Node.js 18+ installed
- Backend server running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
npm run preview
```

## Usage

1. Open `http://localhost:5173` in your browser
2. Type your message in the input field at the bottom
3. Press **Enter** to send (or click the send button)
4. Use **Shift+Enter** to add a new line without sending

## API Integration

The frontend connects to the backend API at:
- **Endpoint**: `http://localhost:8080/api/llm/chat`
- **Method**: POST
- **Body**: `{ "prompt": "your message" }`

## Customization

### Change Theme Colors

Edit the theme in `src/App.tsx`:

```typescript
const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#10a37f', // Change this color
    },
    // ...
  },
})
```

### Change Backend URL

Update the fetch URL in `src/App.tsx`:

```typescript
const response = await fetch('YOUR_BACKEND_URL/api/llm/chat', {
  // ...
})
```

## License

MIT
