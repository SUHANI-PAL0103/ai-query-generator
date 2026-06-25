import { useState } from 'react';
import { useApp } from '../context/AppContext';
import { queryService } from '../services/api';
import { useUser } from '@clerk/react';
import { Database, CheckCircle, XCircle, Loader2, Server } from 'lucide-react';

export default function DatabaseSetup({ onConnected }) {
  const { user } = useUser();
  const { dispatch } = useApp();
  const [host, setHost] = useState('localhost');
  const [port, setPort] = useState('5432');
  const [database, setDatabase] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState(null);
  const [error, setError] = useState('');

  const handleTest = async () => {
    if (!host || !username || !password) {
      setError('Please fill in host, username, and password');
      return;
    }
    setTesting(true);
    setError('');
    setTestResult(null);

    try {
      const config = {
        url: `jdbc:postgresql://${host}:${port}/${database || 'ai_sql_assistant'}`,
        username,
        password,
      };
      const result = await queryService.testDatabaseConnection(config);
      setTestResult(result);
      if (result.success) {
        setError('');
      } else {
        setError(result.message || 'Connection failed');
      }
    } catch (err) {
      setTestResult({ success: false });
      setError('Backend not reachable. Make sure backend is running.');
    } finally {
      setTesting(false);
    }
  };

  const handleConnect = () => {
    if (!host || !username || !password) {
      setError('Please fill in host, username, and password');
      return;
    }
    dispatch({
      type: 'SET_DB_CONNECTION',
      payload: { host, port, database: database || 'ai_sql_assistant', username, password }
    });
    onConnected();
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-blue-50 dark:from-gray-950 dark:to-gray-900 p-4">
      <div className="w-full max-w-lg">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-500 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-green-500/20">
              <Database className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-xl font-bold text-gray-800 dark:text-gray-200">Connect Your Database</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
              Enter your PostgreSQL database credentials to get started
            </p>
            <div className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full">
              <Server className="w-3 h-3 text-gray-400" />
              <span className="text-xs text-gray-500 dark:text-gray-400">{user?.primaryEmailAddress?.emailAddress}</span>
            </div>
          </div>

          {/* Form */}
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Host</label>
                <input
                  type="text"
                  value={host}
                  onChange={(e) => setHost(e.target.value)}
                  placeholder="localhost"
                  className="w-full px-3 py-2.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-600 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Port</label>
                <input
                  type="text"
                  value={port}
                  onChange={(e) => setPort(e.target.value)}
                  className="w-full px-3 py-2.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-600 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Database Name</label>
              <input
                type="text"
                value={database}
                onChange={(e) => setDatabase(e.target.value)}
                placeholder="ai_sql_assistant"
                className="w-full px-3 py-2.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-600 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="postgres"
                className="w-full px-3 py-2.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-600 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                className="w-full px-3 py-2.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-600 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500"
              />
            </div>

            {error && (
              <div className="flex items-center gap-2 p-3 bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 rounded-xl text-sm text-red-600 dark:text-red-400">
                <XCircle className="w-4 h-4 shrink-0" />
                {error}
              </div>
            )}

            {testResult?.success && (
              <div className="flex items-center gap-2 p-3 bg-green-50 dark:bg-green-500/10 border border-green-200 dark:border-green-500/20 rounded-xl text-sm text-green-600 dark:text-green-400">
                <CheckCircle className="w-4 h-4 shrink-0" />
                Connection successful!
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <button
                onClick={handleTest}
                disabled={testing}
                className="flex-1 px-4 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 transition-all duration-200 cursor-pointer"
              >
                {testing ? (
                  <span className="flex items-center justify-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Testing...
                  </span>
                ) : 'Test Connection'}
              </button>
              <button
                onClick={handleConnect}
                disabled={testing}
                className="flex-1 px-4 py-2.5 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-xl text-sm font-medium hover:shadow-lg hover:shadow-green-500/25 disabled:opacity-50 transition-all duration-200 cursor-pointer"
              >
                Connect
              </button>
            </div>
          </div>

          <p className="mt-6 text-xs text-center text-gray-400 dark:text-gray-500">
            Your credentials are stored locally and sent only to your database
          </p>
        </div>
      </div>
    </div>
  );
}