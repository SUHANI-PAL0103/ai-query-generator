import { motion } from 'framer-motion';
import { Database, AlertTriangle, Activity, Table2 } from 'lucide-react';

export default function ImpactAnalysis({ query }) {
  if (!query) return null;

  const riskColors = {
    Low: 'bg-green-500',
    Medium: 'bg-yellow-500',
    High: 'bg-red-500',
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
    >
      <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
        <Activity className="w-4 h-4 text-orange-500" />
        Impact Analysis
      </h3>

      <div className="grid grid-cols-2 gap-4">
        <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800/80 border border-gray-100 dark:border-gray-700/50">
          <p className="text-xs text-gray-500 mb-1">Estimated Rows</p>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            {query.rowsReturned}
          </p>
        </div>
        <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800/80 border border-gray-100 dark:border-gray-700/50">
          <p className="text-xs text-gray-500 mb-1">Query Cost</p>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            {query.cost}
          </p>
        </div>
        <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800/80 border border-gray-100 dark:border-gray-700/50">
          <p className="text-xs text-gray-500 mb-1">Affected Tables</p>
          <div className="flex flex-wrap gap-2 mt-1">
            {query.tables?.map((table, i) => (
              <span
                key={i}
                className="inline-flex items-center gap-1 px-2 py-1 rounded-lg bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 text-xs font-medium"
              >
                <Table2 className="w-3 h-3" />
                {table}
              </span>
            ))}
          </div>
        </div>
        <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800/80 border border-gray-100 dark:border-gray-700/50">
          <p className="text-xs text-gray-500 mb-1">Risk Level</p>
          <div className="flex items-center gap-2 mt-1">
            <div
              className={`w-3 h-3 rounded-full ${riskColors[query.risk]}`}
            />
            <span className="text-lg font-bold text-gray-800 dark:text-gray-200">
              {query.risk}
            </span>
          </div>
        </div>
      </div>
    </motion.div>
  );
}