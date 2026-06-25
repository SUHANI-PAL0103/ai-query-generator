import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useApp } from '../context/AppContext';
import { queryService } from '../services/api';
import {
  Search,
  Trash2,
  RotateCcw,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

const statusIcons = {
  generated: Clock,
  executed: CheckCircle,
  failed: XCircle,
  success: CheckCircle,
  error: XCircle,
};

const statusColors = {
  generated: 'bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400',
  executed: 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400',
  failed: 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400',
  success: 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400',
  error: 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400',
};

export default function History() {
  const { state, dispatch } = useApp();
  const { queryHistory } = state;
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [loading, setLoading] = useState(false);

  const filteredHistory = queryHistory.filter((item) => {
    const matchesSearch =
      (item.prompt || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
      (item.query || '').toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' || item.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const refreshHistory = async () => {
    setLoading(true);
    try {
      const res = await queryService.getQueryHistory();
      if (res.success && Array.isArray(res.data)) {
        dispatch({ type: 'SET_QUERY_HISTORY', payload: res.data });
      }
    } catch (e) {
      console.error('Failed to refresh history:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (index) => {
    dispatch({ type: 'DELETE_HISTORY_ITEM', payload: index });
  };

  const handleClearAll = () => {
    if (window.confirm('Clear all query history?')) {
      dispatch({ type: 'CLEAR_HISTORY' });
    }
  };

  const formatDate = (item) => {
    const raw = item.createdAt || item.timestamp || item.date;
    if (!raw) return 'Invalid Date';
    const d = new Date(raw);
    if (isNaN(d.getTime())) return 'Invalid Date';
    return d.toLocaleString();
  };

  const getStatus = (item) => {
    const raw = item.status || item.queryType || 'generated';
    const normalized = String(raw).toLowerCase();
    if (['success', 'executed', 'select', 'insert', 'update'].includes(normalized)) {
      return 'executed';
    }
    if (['error', 'failed', 'fail'].includes(normalized)) {
      return 'failed';
    }
    return 'generated';
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            Query History
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            View and manage your past queries
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={refreshHistory}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 disabled:opacity-50 transition-all duration-200"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            {loading ? 'Refreshing...' : 'Refresh'}
          </button>
          {queryHistory.length > 0 && (
            <button
              onClick={handleClearAll}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 text-sm font-medium hover:bg-red-100 dark:hover:bg-red-500/20 transition-all duration-200"
            >
              <Trash2 className="w-4 h-4" />
              Clear All
            </button>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search history..."
            className="w-full pl-10 pr-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
          />
        </div>
        <div className="flex items-center gap-2">
          {['all', 'generated', 'executed', 'failed'].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 capitalize ${
                statusFilter === status
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
              }`}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {/* History List */}
      {filteredHistory.length === 0 ? (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center py-16"
        >
          <Clock className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-4" />
          <p className="text-gray-500 dark:text-gray-400 text-sm">
            {queryHistory.length === 0
              ? 'No query history yet. Generate some queries to see them here.'
              : 'No results match your search criteria.'}
          </p>
        </motion.div>
      ) : (
        <div className="space-y-3">
          {filteredHistory.map((item, index) => {
            const StatusIcon = statusIcons[getStatus(item)] || AlertCircle;
            const originalIndex = queryHistory.indexOf(item);

            return (
              <motion.div
                key={item.id || index}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5 hover:shadow-md transition-all duration-200"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800 dark:text-gray-200 mb-2 truncate">
                      {item.prompt}
                    </p>
                    {item.query && (
                      <code className="text-xs bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-gray-600 dark:text-gray-400 font-mono block max-w-full truncate mb-3">
                        {item.query}
                      </code>
                    )}
                    <div className="flex items-center gap-3 text-xs text-gray-500">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {formatDate(item)}
                      </span>
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[getStatus(item)]}`}
                      >
                        <StatusIcon className="w-3 h-3" />
                        {getStatus(item)}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      onClick={() => {
                        window.location.href = '/query-generator';
                        setTimeout(() => {
                          localStorage.setItem('reRunPrompt', item.prompt);
                        }, 100);
                      }}
                      className="p-2 rounded-lg text-gray-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-500/10 transition-all duration-200"
                      title="Re-run query"
                    >
                      <RotateCcw className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(originalIndex)}
                      className="p-2 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all duration-200"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}