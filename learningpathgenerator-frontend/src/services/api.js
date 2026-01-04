import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const API = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request interceptor - adds JWT token to all requests
API.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('[API] Request with token to:', config.url);
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - handles errors
API.interceptors.response.use(
    (response) => {
        console.log('[API] Response OK:', response.config.url);
        return response;
    },
    (error) => {
        const status = error.response?.status;
        const url = error.config?.url || '';

        console.error('[API] Error:', status, url, error.message);

        // Only redirect to login for 401 errors (unauthorized)
        // Don't redirect for 403 (forbidden) - user is authenticated but lacks permission
        // Don't redirect for auth endpoints
        if (status === 401 && !url.includes('/auth/')) {
            console.log('[API] 401 Unauthorized - clearing token');
            localStorage.removeItem('token');
            localStorage.removeItem('user');

            const currentPath = window.location.pathname;
            if (currentPath !== '/login' && currentPath !== '/register') {
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);

// Auth API endpoints
export const authAPI = {
    login: (credentials) => API.post('/auth/login', credentials),
    register: (userData) => API.post('/auth/register', userData),
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }
};

// Quiz API endpoints
export const quizAPI = {
    getTopics: () => API.get('/quiz/topics'),
    startQuiz: (topic, userId) => API.get(`/quiz/start?topic=${topic}&userId=${userId}`),
    submitQuiz: (submission, userId) => API.post(`/quiz/submit?userId=${userId}`, submission),
    addQuestion: (question) => API.post('/quiz/add', question),
};

// Gamification API endpoints
export const gamificationAPI = {
    getStats: () => API.get('/gamification/stats'),
    getLeaderboard: () => API.get('/gamification/leaderboard'),
};

// Learning Path API endpoints
export const learningPathAPI = {
    getUserPaths: () => API.get('/learning-paths'),
    getPathById: (id) => API.get(`/learning-paths/${id}`),
    createPath: (data) => API.post('/learning-paths', data),
    updateProgress: (pathId, resourceId, progress) =>
        API.put(`/learning-paths/${pathId}/resources/${resourceId}/progress`, { progress }),
    exportPath: (pathId) => API.get(`/learning-paths/export/${pathId}`, { responseType: 'blob' }),
};

// User API endpoints
export const userAPI = {
    getProfile: () => API.get('/users/profile'),
    updateProfile: (data) => API.put('/users/profile', data),
};

// Admin API endpoints
export const adminAPI = {
    // Topics
    getTopics: () => API.get('/admin/topics'),
    createTopic: (topic) => API.post('/admin/topics', topic),
    updateTopic: (id, topic) => API.put(`/admin/topics/${id}`, topic),
    deleteTopic: (id) => API.delete(`/admin/topics/${id}`),

    // Questions
    getQuestions: () => API.get('/admin/questions'),
    addQuestion: (question) => API.post('/admin/questions', question),
    updateQuestion: (questionId, question) => API.put(`/admin/questions/${questionId}`, question),
    deleteQuestion: (questionId) => API.delete(`/admin/questions/${questionId}`),

    // Users
    getUsers: () => API.get('/admin/users'),
    updateUserRole: (userId, role) => API.put(`/admin/users/${userId}/role`, { role }),
    deleteUser: (userId) => API.delete(`/admin/users/${userId}`),

    // Stats
    getStats: () => API.get('/admin/stats'),

    // Learning Paths
    getAllPaths: () => API.get('/admin/learning-paths'),
};

export default API;

