interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

interface AuthResponse {
  token: string | null;
  username: string;
  email: string;
  role: string;
}

interface LoginRequest {
  username: string;
  password: string;
}

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: 'Login failed' }));
    throw new Error(errorData.message || 'Login failed. Please check your credentials.');
  }
  
  const authData: AuthResponse = await response.json();
  
  // Store token in localStorage
  if (authData.token) {
    localStorage.setItem('token', authData.token);
    localStorage.setItem('username', authData.username);
  }
  
  return authData;
};

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data)
  });
  
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: 'Registration failed' }));
    throw new Error(errorData.message || 'Failed to register. Please try again.');
  }
  
  const authData: AuthResponse = await response.json();
  
  // Don't store token yet - user needs to confirm email first
  // Token will be null until email is confirmed
  
  return authData;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
};

export const getToken = (): string | null => {
  return localStorage.getItem('token');
};

export const isAuthenticated = (): boolean => {
  return !!getToken();
};
