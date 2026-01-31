import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  Alert,
  IconButton,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { register } from '../services/authService';

interface RegisterDialogProps {
  open: boolean;
  onClose: () => void;
  onRegisterSuccess: (username: string) => void;
  onSwitchToLogin: () => void;
}

export default function RegisterDialog({ open, onClose, onRegisterSuccess, onSwitchToLogin }: RegisterDialogProps) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleRegister = async () => {
    // Validation
    if (!username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      setError('Please fill in all fields');
      return;
    }

    if (username.length < 3 || username.length > 20) {
      setError('Username must be between 3 and 20 characters');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (!email.includes('@')) {
      setError('Please enter a valid email address');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await register({ username, email, password });
      // Show success message - user needs admin approval
      setSuccess(true);
      // Notify parent component
      onRegisterSuccess(response.username);
      // Reset form
      setUsername('');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleRegister();
    }
  };

  const handleClose = () => {
    setError('');
    setSuccess(false);
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          bgcolor: 'background.paper',
          borderRadius: 2,
        }
      }}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <PersonAddIcon color="primary" />
          <Typography variant="h6">Register</Typography>
        </Box>
        <IconButton onClick={handleClose} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          {error && (
            <Alert severity="error" onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          {success && (
            <Alert severity="success" onClose={() => setSuccess(false)}>
              Registration successful! Your account is pending admin approval. You will receive an email once approved.
            </Alert>
          )}

          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
            autoFocus
            helperText="3-20 characters"
          />

          <TextField
            label="Email"
            type="email"
            variant="outlined"
            fullWidth
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
          />

          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
            helperText="At least 6 characters"
          />

          <TextField
            label="Confirm Password"
            type="password"
            variant="outlined"
            fullWidth
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
          />

          <Typography variant="body2" color="text.secondary" textAlign="center">
            Already have an account?{' '}
            <Button
              variant="text"
              size="small"
              onClick={onSwitchToLogin}
              sx={{ textTransform: 'none', p: 0, minWidth: 'auto' }}
            >
              Login here
            </Button>
          </Typography>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        {success ? (
          <Button onClick={handleClose} fullWidth variant="contained">
            Close
          </Button>
        ) : (
          <>
            <Button onClick={handleClose} disabled={loading}>
              Cancel
            </Button>
            <Button
              variant="contained"
              onClick={handleRegister}
              disabled={loading || !username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()}
              startIcon={<PersonAddIcon />}
            >
              {loading ? 'Registering...' : 'Register'}
            </Button>
          </>
        )}
      </DialogActions>
    </Dialog>
  );
}
