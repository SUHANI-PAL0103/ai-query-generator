import { useState } from 'react';
import { motion } from 'framer-motion';
import { useApp } from '../context/AppContext';
import { queryService } from '../services/api';
import {
  Database,
  Sun,
  Moon,
  Monitor,
  Save,
  Loader2,
  CheckCircle,
  XCircle,
  Plug,
  Shield,
  Eye,
  EyeOff,
} from 'lucide-react';

export default function Settings() {
  const { state, dispatch } = useApp();
  const { theme, dbConnection } = state;

  const [connection, setConnection] = useState(
    dbConnection || {
      host: 'localhost',
      port: '5432',
      database: 'ai_sql_assistant',
      username: 'admin',
      password: '',
    }
  );
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState(null);
  const [saved, setSaved] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleTestConnection = async () => {
    setTesting(true);
    setTestResult(null);
    try {
      const result = await queryService.testDatabaseConnection(connection);
      if (result.success) {
        const dbName = result.data?.database || connection.database;
        setTestResult({
          success: true,
          message: result.data?.message || `Connected to database "${dbName}" successfully`,
        });
      } else {
        setTestResult({
          success: false,
          message: result.message || 'Connection failed. Check your credentials.',
        });
      }
    } catch (error) {
      setTestResult({ success: false, message: 'Connection failed.' });
    } finally {
      setTesting(false);
    }
  };

  const handleSaveConnection = () => {
    dispatch({ type: 'SET_DB_CONNECTION', payload: connection });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">
          Settings
        </h1>
        <p className="text-sm text-gray-500 mt-1">
          Configure your database connection and preferences
        </p>
      </div>

      {/* Database Connection */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
      >
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center">
            <Database className="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-gray-800 dark:text-gray-200">
              Database Connection
            </h2>
            <p className="text-sm text-gray-500">
              Configure your database credentials
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Host
            </label>
            <input
              type="text"
              value={connection.host}
              onChange={(e) =>
                setConnection({ ...connection, host: e.target.value })
              }
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Port
            </label>
            <input
              type="text"
              value={connection.port}
              onChange={(e) =>
                setConnection({ ...connection, port: e.target.value })
              }
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Database
            </label>
            <input
              type="text"
              value={connection.database}
              onChange={(e) =>
                setConnection({ ...connection, database: e.target.value })
              }
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Username
            </label>
            <input
              type="text"
              value={connection.username}
              onChange={(e) =>
                setConnection({ ...connection, username: e.target.value })
              }
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
            />
          </div>
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Password
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={connection.password}
                onChange={(e) =>
                  setConnection({ ...connection, password: e.target.value })
                }
                className="w-full px-3 py-2 pr-10 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
              />
              <button
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Test Result */}
        {testResult && (
          <div
            className={`mt-4 flex items-center gap-3 p-4 rounded-xl ${
              testResult.success
                ? 'bg-green-50 dark:bg-green-500/5 border border-green-200 dark:border-green-500/20'
                : 'bg-red-50 dark:bg-red-500/5 border border-red-200 dark:border-red-500/20'
            }`}
          >
            {testResult.success ? (
              <CheckCircle className="w-5 h-5 text-green-500 shrink-0" />
            ) : (
              <XCircle className="w-5 h-5 text-red-500 shrink-0" />
            )}
            <p
              className={`text-sm ${
                testResult.success
                  ? 'text-green-700 dark:text-green-400'
                  : 'text-red-700 dark:text-red-400'
              }`}
            >
              {testResult.message}
            </p>
          </div>
        )}

        {/* Actions */}
        <div className="flex items-center gap-3 mt-6">
          <button
            onClick={handleTestConnection}
            disabled={testing}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 transition-all duration-200"
          >
            {testing ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Plug className="w-4 h-4" />
            )}
            {testing ? 'Testing...' : 'Test Connection'}
          </button>
          <button
            onClick={handleSaveConnection}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 text-white text-sm font-medium hover:shadow-lg hover:shadow-blue-500/20 transition-all duration-200"
          >
            {saved ? (
              <CheckCircle className="w-4 h-4" />
            ) : (
              <Save className="w-4 h-4" />
            )}
            {saved ? 'Saved!' : 'Save'}
          </button>
        </div>
      </motion.div>

      {/* Theme Settings */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
      >
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center">
            <Monitor className="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-gray-800 dark:text-gray-200">
              Theme Settings
            </h2>
            <p className="text-sm text-gray-500">
              Customize your appearance
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <button
            onClick={() => dispatch({ type: 'SET_THEME', payload: 'light' })}
            className={`p-6 rounded-xl border-2 transition-all duration-200 ${
              theme === 'light'
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-500/5'
                : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
            }`}
          >
            <Sun className="w-8 h-8 text-yellow-500 mb-3" />
            <p className="text-sm font-semibold text-gray-800 dark:text-gray-200">
              Light Mode
            </p>
            <p className="text-xs text-gray-500 mt-1">Bright and clean</p>
            {theme === 'light' && (
              <div className="mt-3 w-2 h-2 rounded-full bg-blue-500 mx-auto" />
            )}
          </button>
          <button
            onClick={() => dispatch({ type: 'SET_THEME', payload: 'dark' })}
            className={`p-6 rounded-xl border-2 transition-all duration-200 ${
              theme === 'dark'
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-500/5'
                : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
            }`}
          >
            <Moon className="w-8 h-8 text-blue-400 mb-3" />
            <p className="text-sm font-semibold text-gray-800 dark:text-gray-200">
              Dark Mode
            </p>
            <p className="text-xs text-gray-500 mt-1">Easy on the eyes</p>
            {theme === 'dark' && (
              <div className="mt-3 w-2 h-2 rounded-full bg-blue-500 mx-auto" />
            )}
          </button>
        </div>
      </motion.div>

      {/* Security Info */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
      >
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-green-500 to-emerald-500 flex items-center justify-center">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-gray-800 dark:text-gray-200">
              Security & Privacy
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              All credentials are stored locally in your browser. No data is sent
              to external servers.
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}