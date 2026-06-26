import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import { useUser, useAuth, SignInButton, SignUpButton, UserButton } from '@clerk/react';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import QueryGenerator from './pages/QueryGenerator';
import History from './pages/History';
import Schema from './pages/Schema';
import Settings from './pages/Settings';
import AdminPanel from './pages/AdminPanel';
import DatabaseSetup from './pages/DatabaseSetup';
import { useEffect, useState } from 'react';
import { queryService } from './services/api';

function AuthLoading() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-blue-50 dark:from-gray-950 dark:to-gray-900">
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p className="text-gray-500 dark:text-gray-400">Loading...</p>
      </div>
    </div>
  );
}

function SignInPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-blue-50 dark:from-gray-950 dark:to-gray-900 p-4">
      <div className="w-full max-w-md">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-8 text-center">
          <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-lg shadow-blue-500/20">
            <svg className="w-10 h-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200 mb-2">AI SQL Query Generator</h1>
          <p className="text-gray-500 dark:text-gray-400 mb-2">Generate, execute, and manage SQL queries with AI</p>
          <p className="text-xs text-gray-400 dark:text-gray-500 mb-8">Sign in to get started</p>
          
          <div className="flex flex-col gap-3">
            <SignInButton mode="modal">
              <button className="w-full px-6 py-3 bg-gradient-to-r from-blue-500 to-cyan-500 text-white rounded-xl font-medium hover:shadow-lg hover:shadow-blue-500/25 transition-all duration-200 cursor-pointer">
                Sign In
              </button>
            </SignInButton>
            <SignUpButton mode="modal">
              <button className="w-full px-6 py-3 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-all duration-200 cursor-pointer">
                Create Account
              </button>
            </SignUpButton>
          </div>
        </div>
      </div>
    </div>
  );
}

function AppContent() {
  const { isLoaded, isSignedIn, user } = useUser();
  const { getToken } = useAuth();
  const { state, dispatch } = useApp();

  const [dbConnected, setDbConnected] = useState(Boolean(state.dbConnection));

  useEffect(() => {
    if (isLoaded && isSignedIn && user) {
      getToken().then(token => {
        if (token) {
          localStorage.setItem('clerkToken', token);
          localStorage.setItem('clerkUserId', user.id);
          localStorage.setItem('clerkUserEmail', user.primaryEmailAddress?.emailAddress || '');
          localStorage.setItem('clerkUserName', user.fullName || '');
        }
      });
      // Register user silently and check admin status
      queryService.registerUser({
        clerkId: user.id,
        email: user.primaryEmailAddress?.emailAddress,
        name: user.fullName,
        imageUrl: user.imageUrl
      }).then((res) => {
        if (res && res.isAdmin !== undefined) {
          dispatch({ type: 'SET_IS_ADMIN', payload: res.isAdmin });
        }
      }).catch(() => {});
    }
  }, [isLoaded, isSignedIn, user, dispatch]);

  useEffect(() => {
    setDbConnected(Boolean(state.dbConnection));
  }, [state.dbConnection]);

  if (!isLoaded) return <AuthLoading />;
  if (!isSignedIn) return <SignInPage />;
  if (!dbConnected) return <DatabaseSetup onConnected={() => setDbConnected(true)} />;

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50 dark:from-gray-950 dark:to-gray-900 text-gray-900 dark:text-gray-100 transition-colors duration-200">
      <Sidebar />
      <Navbar />
      <main className="pt-16 min-h-screen transition-all duration-300 ml-[280px]">
        <div className="p-6 max-w-7xl mx-auto">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/query-generator" element={<QueryGenerator />} />
            <Route path="/history" element={<History />} />
            <Route path="/schema" element={<Schema />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="/admin" element={<AdminPanel />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

function App() {
  return (
    <AppProvider>
      <Router>
        <AppContent />
      </Router>
    </AppProvider>
  );
}

export default App;
