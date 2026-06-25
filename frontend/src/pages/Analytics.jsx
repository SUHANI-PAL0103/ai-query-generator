import { motion } from 'framer-motion';
import { QueryTypePie, QueryTrendLine, RiskBarChart } from '../components/Charts';
import { BarChart3, TrendingUp, TrendingDown, Activity } from 'lucide-react';

const summaryStats = [
  {
    label: 'Total Queries (All Time)',
    value: '1,847',
    change: '+12.5%',
    trend: 'up',
    icon: BarChart3,
  },
  {
    label: 'Avg. Query Complexity',
    value: '2.4',
    change: 'Moderate',
    trend: 'up',
    icon: Activity,
  },
  {
    label: 'Success Rate',
    value: '94.2%',
    change: '+3.1%',
    trend: 'up',
    icon: TrendingUp,
  },
  {
    label: 'Avg. Execution Time',
    value: '1.8s',
    change: '-0.3s',
    trend: 'down',
    icon: TrendingDown,
  },
];

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

export default function Analytics() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">
          Analytics
        </h1>
        <p className="text-sm text-gray-500 mt-1">
          Detailed insights into query patterns and performance
        </p>
      </div>

      {/* Summary Stats */}
      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4"
      >
        {summaryStats.map((stat, i) => (
          <motion.div
            key={i}
            variants={item}
            className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-5"
          >
            <div className="flex items-center gap-3 mb-3">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center">
                <stat.icon className="w-4 h-4 text-white" />
              </div>
              <span
                className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                  stat.trend === 'up'
                    ? 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400'
                    : 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                }`}
              >
                {stat.change}
              </span>
            </div>
            <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
              {stat.value}
            </p>
            <p className="text-xs text-gray-500 mt-1">{stat.label}</p>
          </motion.div>
        ))}
      </motion.div>

      {/* Charts Grid */}
      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="grid grid-cols-1 lg:grid-cols-2 gap-6"
      >
        <motion.div variants={item}>
          <QueryTypePie />
        </motion.div>
        <motion.div variants={item}>
          <RiskBarChart />
        </motion.div>
        <motion.div variants={item} className="lg:col-span-2">
          <QueryTrendLine />
        </motion.div>
      </motion.div>

      {/* Additional Insights */}
      <motion.div
        variants={item}
        initial="hidden"
        animate="show"
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
      >
        <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-4">
          Key Insights
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-blue-50 dark:bg-blue-500/5 border border-blue-100 dark:border-blue-500/10">
            <p className="text-sm font-medium text-blue-600 dark:text-blue-400 mb-1">
              Most Used Table
            </p>
            <p className="text-lg font-bold text-blue-700 dark:text-blue-300">
              Employee
            </p>
            <p className="text-xs text-blue-500 mt-1">Used in 45% of queries</p>
          </div>
          <div className="p-4 rounded-xl bg-green-50 dark:bg-green-500/5 border border-green-100 dark:border-green-500/10">
            <p className="text-sm font-medium text-green-600 dark:text-green-400 mb-1">
              Most Common Pattern
            </p>
            <p className="text-lg font-bold text-green-700 dark:text-green-300">
              SELECT + WHERE
            </p>
            <p className="text-xs text-green-500 mt-1">Present in 62% of queries</p>
          </div>
          <div className="p-4 rounded-xl bg-purple-50 dark:bg-purple-500/5 border border-purple-100 dark:border-purple-500/10">
            <p className="text-sm font-medium text-purple-600 dark:text-purple-400 mb-1">
              Peak Usage Time
            </p>
            <p className="text-lg font-bold text-purple-700 dark:text-purple-300">
              10:00 AM - 2:00 PM
            </p>
            <p className="text-xs text-purple-500 mt-1">Highest query volume</p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}