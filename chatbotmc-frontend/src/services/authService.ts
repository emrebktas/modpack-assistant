interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
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
    throw new Error('Login failed. Please check your credentials.');
  }
  
  const authData: AuthResponse = await response.json();
  
  // Store token in localStorage
  localStorage.setItem('token', authData.token);
  localStorage.setItem('username', authData.username);
  
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
    throw new Error('Failed to register. Please try again.');
  }
  
  const authData: AuthResponse = await response.json();
  
  // Store token in localStorage
  localStorage.setItem('token', authData.token);
  localStorage.setItem('username', authData.username);
  
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
