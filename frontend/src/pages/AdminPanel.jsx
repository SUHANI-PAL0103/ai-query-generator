import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { queryService } from '../services/api';
import { useUser } from '@clerk/react';
import {
  Users,
  Database,
  BarChart3,
  Clock,
  Shield,
  Mail,
  User as UserIcon,
} from 'lucide-react';

export default function AdminPanel() {
  const { user } = useUser();
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({ totalUsers: 0, totalQueries: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [usersRes, statsRes] = await Promise.all([
        queryService.getAdminUsers(),
        queryService.getAdminStats(),
      ]);

      if (!usersRes.success || !statsRes.success) {
        setError('Access denied or admin panel unavailable');
        setLoading(false);
        return;
      }

      setUsers(usersRes.data.users || []);
      setStats(statsRes.data || { totalUsers: 0, totalQueries: 0 });
    } catch (err) {
      console.error('Admin fetch error:', err);
      setError('Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 rounded-2xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20">
        <h2 className="text-lg font-semibold text-red-600 dark:text-red-400">Access Restricted</h2>
        <p className="text-sm text-red-500 mt-1">This panel is for administrators only.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">Admin Panel</h1>
        <p className="text-sm text-gray-500 mt-1">User registry and query statistics</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center">
              <Users className="w-5 h-5 text-white" />
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">{stats.totalUsers}</p>
          <p className="text-sm text-gray-500">Total Users</p>
        </div>

        <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-green-500 to-emerald-500 flex items-center justify-center">
              <Database className="w-5 h-5 text-white" />
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">{stats.totalQueries}</p>
          <p className="text-sm text-gray-500">Total Queries</p>
        </div>

        <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center">
              <BarChart3 className="w-5 h-5 text-white" />
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">{stats.avgQueriesPerUser || 0}</p>
          <p className="text-sm text-gray-500">Avg Queries/User</p>
        </div>

        <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-orange-500 to-red-500 flex items-center justify-center">
              <Shield className="w-5 h-5 text-white" />
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">{stats.adminCount || 0}</p>
          <p className="text-sm text-gray-500">Admins</p>
        </div>
      </div>

      {/* Users Table */}
      <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 overflow-hidden">
        <div className="p-5 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200">
            Registered Users
          </h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 dark:bg-gray-800/80">
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">User</th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">Email</th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">Queries</th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">Role</th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">Registered</th>
                <th className="text-left px-5 py-3 font-semibold text-gray-700 dark:text-gray-300 text-xs uppercase">Last Active</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700/50">
              {users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-5 py-8 text-center text-sm text-gray-500">
                    No users registered yet
                  </td>
                </tr>
              ) : (
                users.map((u, i) => (
                  <tr key={i} className="hover:bg-gray-50 dark:hover:bg-gray-800/30">
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center">
                          <UserIcon className="w-4 h-4 text-white" />
                        </div>
                        <span className="text-gray-700 dark:text-gray-300 font-medium">{u.name || u.clerkId?.slice(0,8)}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-gray-500">{u.email}</td>
                    <td className="px-5 py-3">
                      <span className="px-2 py-1 bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 rounded-full text-xs font-medium">
                        {u.totalQueriesGenerated || 0}
                      </span>
                    </td>
                    <td className="px-5 py-3">
                      {u.isAdmin ? (
                        <span className="px-2 py-1 bg-purple-100 dark:bg-purple-500/10 text-purple-600 dark:text-purple-400 rounded-full text-xs font-medium">Admin</span>
                      ) : (
                        <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-500 rounded-full text-xs">User</span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-xs text-gray-500">{u.registeredAt ? new Date(u.registeredAt).toLocaleDateString() : '-'}</td>
                    <td className="px-5 py-3 text-xs text-gray-500">{u.lastActiveAt ? new Date(u.lastActiveAt).toLocaleDateString() : '-'}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}