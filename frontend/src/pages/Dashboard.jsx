import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useApp } from '../context/AppContext';
import { QueryTypePie, QueryTrendLine } from '../components/Charts';
import { queryService } from '../services/api';
import {
  Terminal,
  CheckCircle,
  Clock,
  Bookmark,
  ArrowUpRight,
  ArrowDownRight,
  RefreshCw,
} from 'lucide-react';

const emptyStats = [
  {
    label: 'Total Queries',
    value: '—',
    change: '—',
    trend: 'up',
    icon: Terminal,
    gradient: 'from-blue-500 to-cyan-500',
  },
  {
    label: 'Successful Executions',
    value: '—',
    change: '—',
    trend: 'up',
    icon: CheckCircle,
    gradient: 'from-green-500 to-emerald-500',
  },
  {
    label: 'Avg. Execution Time',
    value: '—',
    change: '—',
    trend: 'down',
    icon: Clock,
    gradient: 'from-orange-500 to-red-500',
  },
  {
    label: 'Saved Queries',
    value: '—',
    change: '—',
    trend: 'up',
    icon: Bookmark,
    gradient: 'from-purple-500 to-pink-500',
  },
];

const emptyRecentActivity = [];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.1 },
  },
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0 },
};

export default function Dashboard() {
  const { state } = useApp();
  const [stats, setStats] = useState(emptyStats);
  const [recentActivity, setRecentActivity] = useState(emptyRecentActivity);
  const [queryTypeChartData, setQueryTypeChartData] = useState([]);
  const [riskChartData, setRiskChartData] = useState([]);
  const [trendChartData, setTrendChartData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsRes, activityRes] = await Promise.all([
        queryService.getDashboardStats(),
        queryService.getRecentActivity(),
      ]);

      const statsData = statsRes.success ? statsRes.data : {};
      const activityData = activityRes.success ? activityRes.data : [];

      const totalQueries = statsData.totalQueries ?? 0;
      const successfulExecutions = statsData.successfulExecutions ?? 0;
      const avgExec = statsData.avgExecutionTime ?? 'N/A';
      const savedQueries = statsData.savedQueries ?? 0;
      const successRate = statsData.successRate ?? 0;

      const typeMap = statsData.queryTypeDistribution || {};
      const riskMap = statsData.riskDistribution || {};
      const trendMap = statsData.trendData || {};

      const TYPE_COLORS = {
        SELECT: '#3B82F6',
        UPDATE: '#8B5CF6',
        DELETE: '#EF4444',
        INSERT: '#10B981',
        CREATE: '#F59E0B',
        DROP: '#EC4899',
      };

      const queryTypeChart = Object.entries(typeMap)
        .filter(([, value]) => typeof value === 'number')
        .map(([name, value]) => ({
          name,
          value,
          color: TYPE_COLORS[name] || '#6B7280',
        }));

      const riskColorMap = {
        LOW: '#10B981',
        MEDIUM: '#F59E0B',
        HIGH: '#EF4444',
      };

      const riskChart = Object.entries(riskMap)
        .filter(([, value]) => typeof value === 'number')
        .map(([name, value]) => ({
          name: name ? `${name} Risk` : 'Unknown',
          count: value,
          color: riskColorMap[name] || '#6B7280',
        }));

      const trendChart = Object.entries(trendMap)
        .filter(([, value]) => typeof value === 'number')
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([label, value]) => ({
          label,
          queries: value,
          executions: value,
        }));

      setStats([
        {
          label: 'Total Queries',
          value: totalQueries.toString(),
          change: successRate ? `${successRate}% success` : '—',
          trend: 'up',
          icon: Terminal,
          gradient: 'from-blue-500 to-cyan-500',
        },
        {
          label: 'Successful Executions',
          value: successfulExecutions.toString(),
          change: totalQueries > 0 ? `${successRate}%` : '—',
          trend: 'up',
          icon: CheckCircle,
          gradient: 'from-green-500 to-emerald-500',
        },
        {
          label: 'Avg. Execution Time',
          value: avgExec.toString(),
          change: '—',
          trend: 'down',
          icon: Clock,
          gradient: 'from-orange-500 to-red-500',
        },
        {
          label: 'Saved Queries',
          value: savedQueries.toString(),
          change: totalQueries > 0 ? '—' : '—',
          trend: 'up',
          icon: Bookmark,
          gradient: 'from-purple-500 to-pink-500',
        },
      ]);

      setRecentActivity(
        activityData.map((item) => ({
          prompt: item.prompt,
          query: item.query,
          date: item.date,
          status: item.status || 'success',
        }))
      );

      setQueryTypeChartData(queryTypeChart);
      setRiskChartData(riskChart);
      setTrendChartData(trendChart);
    } catch (err) {
      console.error('Error loading dashboard:', err);
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            Dashboard
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Real-time overview of your connected database and AI SQL assistant
          </p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 disabled:opacity-50 transition-all duration-200"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          {loading ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 text-sm text-red-600 dark:text-red-400">
          {error}
        </div>
      )}

      {/* Stats Cards */}
      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4"
      >
        {stats.map((stat, i) => (
          <motion.div
            key={i}
            variants={item}
            className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5 hover:shadow-md transition-all duration-200"
          >
            <div className="flex items-center justify-between mb-4">
              <div
                className={`w-10 h-10 rounded-xl bg-gradient-to-br ${stat.gradient} flex items-center justify-center`}
              >
                <stat.icon className="w-5 h-5 text-white" />
              </div>
              <div
                className={`flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full ${
                  stat.trend === 'up'
                    ? 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400'
                    : 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                }`}
              >
                {stat.trend === 'up' ? (
                  <ArrowUpRight className="w-3 h-3" />
                ) : (
                  <ArrowDownRight className="w-3 h-3" />
                )}
                {stat.change}
              </div>
            </div>
            <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
              {stat.value}
            </p>
            <p className="text-sm text-gray-500 mt-1">{stat.label}</p>
          </motion.div>
        ))}
      </motion.div>

      {/* Charts */}
      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="grid grid-cols-1 lg:grid-cols-2 gap-6"
      >
        <motion.div variants={item} className="min-h-[320px]">
          <QueryTypePie data={queryTypeChartData} />
        </motion.div>
        <motion.div variants={item} className="min-h-[320px]">
          <QueryTrendLine data={trendChartData} />
        </motion.div>
      </motion.div>

      {/* Recent Activity Table */}
      <motion.div
        variants={item}
        initial="hidden"
        animate="show"
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 overflow-hidden"
      >
        <div className="p-5 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200">
            Recent Activity
          </h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 dark:bg-gray-800/80">
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase tracking-wider">
                  Prompt
                </th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase tracking-wider">
                  Generated Query
                </th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase tracking-wider">
                  Date
                </th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase tracking-wider">
                  Status
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700/50">
              {recentActivity.length === 0 ? (
                <tr>
                  <td
                    colSpan={4}
                    className="px-5 py-8 text-center text-sm text-gray-500"
                  >
                    No recent activity. Start by generating or executing a query.
                  </td>
                </tr>
              ) : (
                recentActivity.map((activity, i) => (
                  <tr
                    key={i}
                    className="hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors"
                  >
                    <td className="px-5 py-3 text-gray-700 dark:text-gray-300 max-w-[200px] truncate">
                      {activity.prompt}
                    </td>
                    <td className="px-5 py-3">
                      <code className="text-xs bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-gray-600 dark:text-gray-400 font-mono max-w-[250px] truncate block">
                        {activity.query}
                      </code>
                    </td>
                    <td className="px-5 py-3 text-gray-500 text-xs whitespace-nowrap">
                      {activity.date}
                    </td>
                    <td className="px-5 py-3">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${
                          activity.status === 'success'
                            ? 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400'
                            : 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                        }`}
                      >
                        {activity.status === 'success' ? (
                          <CheckCircle className="w-3 h-3" />
                        ) : (
                          <ArrowDownRight className="w-3 h-3" />
                        )}
                        {activity.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </motion.div>
    </div>
  );
}