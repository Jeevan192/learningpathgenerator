import React, { useState, useCallback, useEffect } from 'react';
import axios from 'axios';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Quiz from './components/Quiz';
import AdminPanel from './components/AdminPanel';
import './App.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [view, setView] = useState('login');
  const [learningPath, setLearningPath] = useState(null);
  const [progress, setProgress] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('currentUser');
    const storedToken = localStorage.getItem('token');
    const storedRole = localStorage.getItem('role');
    
    console.log('Initial load check:', { storedUser, storedToken, storedRole });
    
    if (storedUser && storedToken && storedRole) {
      setCurrentUser(storedUser);
      setToken(storedToken);
      setRole(storedRole);
      setView('dashboard');
      loadUserData(storedUser, storedToken);
    } else {
      setView('login');
    }
    
    setIsLoading(false);
  }, []);

  const loadUserData = useCallback(async (username, userToken) => {
    if (!username || !userToken) return;
    
    console.log('Loading user data for:', username);
    
    // Load progress from backend
    try {
      const progressResp = await axios.get(
        `${API_URL}/api/progress/user/${username}`,
        { headers: { Authorization: `Bearer ${userToken}` } }
      );
      
      if (progressResp.data) {
        console.log('Progress loaded from backend:', progressResp.data);
        setProgress(progressResp.data);
        localStorage.setItem(`progress_${username}`, JSON.stringify(progressResp.data));
      }
    } catch (err) {
      if (err.response?.status === 404) {
        console.log('No progress found in backend for user');
        setProgress(null);
      } else {
        console.error('Error loading progress from backend:', err);
        
        // Fallback to localStorage
        const storedProgress = localStorage.getItem(`progress_${username}`);
        if (storedProgress) {
          try {
            const parsed = JSON.parse(storedProgress);
            console.log('Loaded progress from localStorage:', parsed);
            setProgress(parsed);
          } catch (e) {
            console.error('Error parsing stored progress:', e);
          }
        }
      }
    }
    
    // Load learning path from backend or localStorage
    try {
      const pathResp = await axios.get(
        `${API_URL}/api/user-learning-paths/${username}`,
        { headers: { Authorization: `Bearer ${userToken}` } }
      );
      
      if (pathResp.data) {
        console.log('Learning path loaded from backend:', pathResp.data);
        setLearningPath(pathResp.data);
        localStorage.setItem(`learningPath_${username}`, JSON.stringify(pathResp.data));
      }
    } catch (err) {
      console.log('Learning path not found in backend, checking localStorage');
      
      const storedPath = localStorage.getItem(`learningPath_${username}`);
      if (storedPath) {
        try {
          const parsed = JSON.parse(storedPath);
          console.log('Loaded learning path from localStorage:', parsed);
          setLearningPath(parsed);
        } catch (e) {
          console.error('Error parsing stored learning path:', e);
        }
      }
    }
  }, []);

  const handleLogin = async (username, password) => {
    try {
      const response = await axios.post(`${API_URL}/api/auth/login`, { 
        username, 
        password 
      });
      
      console.log('Login response:', response.data);
      
      const userRole = response.data.role || 'USER';
      const userToken = response.data.token;
      
      setCurrentUser(username);
      setToken(userToken);
      setRole(userRole);
      
      localStorage.setItem('currentUser', username);
      localStorage.setItem('token', userToken);
      localStorage.setItem('role', userRole);
      
      console.log('User logged in:', { username, role: userRole });
      
      // Load user data BEFORE setting view
      await loadUserData(username, userToken);
      
      // Now switch to dashboard
      setView('dashboard');
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || error.response?.data || 'Login failed' 
      };
    }
  };

  const handleRegister = async (username, password, registerAsAdmin = false) => {
    try {
      await axios.post(`${API_URL}/api/auth/register`, { 
        username, 
        password, 
        role: registerAsAdmin ? "ADMIN" : "USER" 
      });
      return { success: true };
    } catch (error) {
      console.error('Registration error:', error);
      return { 
        success: false, 
        message: typeof error.response?.data === 'string' 
          ? error.response.data 
          : 'Registration failed' 
      };
    }
  };

  const handleLogout = () => {
    console.log('Logging out...');
    
    const userToLogout = currentUser;
    
    // DON'T clear user-specific data from localStorage
    // Only clear auth data
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    
    // Keep progress and learning path in localStorage for quick reload
    // They will be refreshed from backend on next login anyway
    
    setCurrentUser(null);
    setToken(null);
    setRole(null);
    setLearningPath(null);
    setProgress(null);
    setView('login');
    
    console.log('Logged out successfully');
  };

  const handlePathGenerated = (newPath, newProgress) => {
    console.log('New path generated:', newPath);
    console.log('New progress generated:', newProgress);
    
    setLearningPath(newPath);
    setProgress(newProgress);
    
    if (currentUser) {
      if (newPath) {
        localStorage.setItem(`learningPath_${currentUser}`, JSON.stringify(newPath));
      }
      if (newProgress) {
        localStorage.setItem(`progress_${currentUser}`, JSON.stringify(newProgress));
      }
    }
    
    setView('dashboard');
  };

  const handleProgressUpdate = async (updatedProgress) => {
    try {
      console.log('Updating progress:', updatedProgress);
      
      const response = await axios.post(
        `${API_URL}/api/progress/update`,
        updatedProgress,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      console.log('Progress updated successfully:', response.data);
      
      setProgress(response.data);
      
      if (currentUser) {
        localStorage.setItem(`progress_${currentUser}`, JSON.stringify(response.data));
      }
      
      return { success: true };
    } catch (error) {
      console.error('Progress update error:', error);
      return { 
        success: false, 
        message: error.response?.data?.error || 'Failed to update progress' 
      };
    }
  };

  if (isLoading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        fontSize: '1.5rem',
        fontWeight: '700'
      }}>
        Loading...
      </div>
    );
  }

  return (
    <div className="App">
      <header className="app-header">
        <div className="header-content">
          <h1 className="app-title">🎓 Learning Path Generator</h1>
          {currentUser && (
            <div className="header-right">
              <nav className="nav-menu" style={{ display: 'flex', gap: '1rem' }}>
                <button 
                  onClick={() => {
                    console.log('Dashboard clicked');
                    setView('dashboard');
                  }} 
                  className={view === 'dashboard' ? 'active' : ''}
                  style={{
                    background: view === 'dashboard' ? 'white' : 'rgba(255, 255, 255, 0.2)',
                    color: view === 'dashboard' ? '#667eea' : 'white',
                    border: '2px solid',
                    borderColor: view === 'dashboard' ? 'white' : 'rgba(255, 255, 255, 0.4)',
                    padding: '0.8rem 1.6rem',
                    borderRadius: '8px',
                    fontWeight: '700',
                    fontSize: '1rem',
                    cursor: 'pointer',
                    transition: 'all 0.3s'
                  }}
                >
                  Dashboard
                </button>
                
                {role === 'ADMIN' && (
                  <button 
                    onClick={() => {
                      console.log('Admin Panel clicked, role:', role);
                      setView('admin');
                    }} 
                    className={view === 'admin' ? 'active' : ''}
                    style={{
                      background: view === 'admin' ? 'white' : 'rgba(255, 255, 255, 0.2)',
                      color: view === 'admin' ? '#667eea' : 'white',
                      border: '2px solid',
                      borderColor: view === 'admin' ? 'white' : 'rgba(255, 255, 255, 0.4)',
                      padding: '0.8rem 1.6rem',
                      borderRadius: '8px',
                      fontWeight: '700',
                      fontSize: '1rem',
                      cursor: 'pointer',
                      transition: 'all 0.3s'
                    }}
                  >
                    Admin Panel
                  </button>
                )}
              </nav>
              
              <div className="user-info" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <span className="user-name" style={{ fontWeight: '700', color: 'white' }}>
                  👤 {currentUser}
                </span>
                <span 
                  className={`user-role ${role === 'ADMIN' ? 'admin' : 'user'}`}
                  style={{
                    padding: '0.6rem 1.2rem',
                    borderRadius: '20px',
                    fontWeight: '800',
                    fontSize: '0.95rem',
                    background: role === 'ADMIN' ? '#fbbf24' : 'rgba(255, 255, 255, 0.25)',
                    color: role === 'ADMIN' ? '#78350f' : 'white',
                    border: role === 'ADMIN' ? '2px solid #f59e0b' : '2px solid rgba(255, 255, 255, 0.4)',
                    boxShadow: role === 'ADMIN' ? '0 3px 10px rgba(251, 191, 36, 0.4)' : 'none'
                  }}
                >
                  {role === 'ADMIN' ? '🛡️ Admin' : '👥 User'}
                </span>
                <button 
                  onClick={handleLogout} 
                  className="logout-btn"
                  style={{
                    background: 'rgba(255, 255, 255, 0.2)',
                    color: 'white',
                    border: '2px solid rgba(255, 255, 255, 0.4)',
                    padding: '0.8rem 1.6rem',
                    borderRadius: '8px',
                    fontWeight: '700',
                    cursor: 'pointer',
                    transition: 'all 0.3s'
                  }}
                >
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </header>
      
      <main className="app-main">
        {view === 'login' && !currentUser && (
          <Login 
            onLogin={handleLogin} 
            onSwitchToRegister={() => setView('register')} 
          />
        )}
        
        {view === 'register' && !currentUser && (
          <Register 
            onRegister={handleRegister} 
            onSwitchToLogin={() => setView('login')} 
          />
        )}
        
        {currentUser && view === 'dashboard' && (
          <Dashboard
            learningPath={learningPath}
            progress={progress}
            role={role}
            onStartQuiz={() => setView('quiz')}
            onProgressUpdate={handleProgressUpdate}
          />
        )}
        
        {currentUser && view === 'quiz' && (
          <Quiz
            currentUser={currentUser}
            token={token}
            onBack={() => setView('dashboard')}
            onPathGenerated={handlePathGenerated}
          />
        )}
        
        {currentUser && view === 'admin' && role === 'ADMIN' && (
          <AdminPanel token={token} role={role} />
        )}
        
        {currentUser && view === 'admin' && role !== 'ADMIN' && (
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '60vh',
            fontSize: '1.5rem',
            color: '#ef4444',
            fontWeight: '700'
          }}>
            ⛔ Access Denied - Admin Only
          </div>
        )}
      </main>
    </div>
  );
}

export default App;