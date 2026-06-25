import { useState } from 'react';
import { motion } from 'framer-motion';
import {
  Copy,
  Check,
  Eye,
  FileText,
  Shield,
  Play,
  ChevronDown,
  ChevronUp,
  Star,
  Table,
  AlertTriangle,
  Activity,
} from 'lucide-react';

export default function QueryCard({ query, onSelect, onExecute, isSelected }) {
  const [copied, setCopied] = useState(false);
  const [expanded, setExpanded] = useState(true);

  const handleCopy = async () => {
    await navigator.clipboard.writeText(query.sql);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const riskColors = {
    Low: 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400 border-green-200 dark:border-green-500/20',
    Medium:
      'bg-yellow-100 dark:bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 border-yellow-200 dark:border-yellow-500/20',
    High: 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400 border-red-200 dark:border-red-500/20',
  };

  const riskIcons = {
    Low: Shield,
    Medium: AlertTriangle,
    High: AlertTriangle,
  };
  const RiskIcon = riskIcons[query.risk] || Shield;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className={`rounded-2xl border-2 transition-all duration-200 ${
        isSelected
          ? 'border-blue-400 dark:border-blue-500 bg-blue-50/50 dark:bg-blue-500/5 shadow-lg shadow-blue-500/10'
          : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 hover:shadow-md hover:border-gray-300 dark:hover:border-gray-600'
      }`}
    >
      {/* Header */}
      <div className="p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2.5">
              <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 text-white text-sm font-bold shadow-md">
                Q
              </div>
              <div>
                <span className="text-base font-semibold text-gray-800 dark:text-gray-200">
                  Query #{query.id}
                </span>
                <div className="flex items-center gap-2 mt-0.5">
                  <RiskIcon className="w-3 h-3 text-gray-400" />
                  <span className="text-xs text-gray-500">{query.tables?.length || 0} table(s)</span>
                </div>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {/* Risk Badge */}
            <span
              className={`inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1 rounded-full border ${riskColors[query.risk] || riskColors.Low}`}
            >
              <RiskIcon className="w-3.5 h-3.5" />
              {query.risk || 'Low'} Risk
            </span>
            {/* Execution Cost */}
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-purple-100 dark:bg-purple-500/10 text-purple-600 dark:text-purple-400 border border-purple-200 dark:border-purple-500/20">
              <Activity className="w-3.5 h-3.5" />
              {query.cost || 'N/A'}
            </span>
            {/* Estimated Rows */}
            {query.rowsReturned !== undefined && query.rowsReturned !== 'N/A' && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-500/20">
                <Table className="w-3.5 h-3.5" />
                ~{query.rowsReturned} rows
              </span>
            )}
          </div>
        </div>

        {/* SQL Code Block - Full Width, Enhanced */}
        <div className="relative group">
          <div className="absolute top-0 left-0 w-full h-10 bg-gradient-to-b from-gray-900/80 to-transparent rounded-t-xl pointer-events-none z-10" />
          <pre className="p-5 rounded-xl bg-[#1e1e2e] dark:bg-[#1a1a2e] text-[#a6e3a1] text-sm font-mono overflow-x-auto border border-gray-700/50 shadow-inner leading-relaxed">
            <code>{query.sql}</code>
          </pre>
          <div className="absolute top-3 right-3 flex items-center gap-2 z-20">
            <span className="text-[10px] text-gray-500 bg-gray-800/80 px-2 py-1 rounded-md font-mono">
              SQL
            </span>
            <button
              onClick={handleCopy}
              className="p-2 rounded-lg bg-gray-800/80 hover:bg-gray-700 text-gray-400 hover:text-white transition-all duration-200"
            >
              {copied ? (
                <Check className="w-4 h-4 text-green-400" />
              ) : (
                <Copy className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>

        {/* Tables Used */}
        {query.tables && query.tables.length > 0 && (
          <div className="flex items-center gap-2 mt-4">
            <Database className="w-4 h-4 text-blue-400" />
            <span className="text-xs text-gray-500 font-medium">Tables:</span>
            <div className="flex flex-wrap gap-1.5">
              {query.tables.map((table, i) => (
                <span
                  key={i}
                  className="inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-mono font-medium bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-500/20"
                >
                  {table}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Actions Bar */}
      <div className="px-6 pb-6">
        <div className="flex items-center gap-3 flex-wrap">
          <button
            onClick={() => onSelect?.(query)}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 ${
              isSelected
                ? 'bg-blue-500 text-white shadow-md shadow-blue-500/20'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
            }`}
          >
            <Star className={`w-4 h-4 ${isSelected ? 'fill-white' : ''}`} />
            {isSelected ? 'Selected' : 'Select Query'}
          </button>
          <button
            onClick={() => setExpanded(!expanded)}
            className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 transition-all duration-200"
          >
            {expanded ? (
              <ChevronUp className="w-4 h-4" />
            ) : (
              <ChevronDown className="w-4 h-4" />
            )}
            {expanded ? 'Hide Details' : 'Show Details'}
          </button>
          <button
            onClick={() => onExecute?.(query)}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-blue-500 to-purple-500 text-white hover:shadow-lg hover:shadow-blue-500/20 transition-all duration-200 ml-auto"
          >
            <Play className="w-4 h-4" />
            Execute Query
          </button>
        </div>

        {/* Expanded Details */}
        {expanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            className="mt-5 pt-5 border-t border-gray-200 dark:border-gray-700 space-y-4"
          >
            {/* Explanation */}
            <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-700/50">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="w-4 h-4 text-blue-500" />
                <span className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Explanation</span>
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
                {query.explanation}
              </p>
            </div>

            {/* Clauses */}
            {query.clauses && query.clauses.length > 0 && (
              <div>
                <span className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2 block">
                  Query Breakdown
                </span>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                  {query.clauses.map((clause, i) => (
                    <div
                      key={i}
                      className="flex items-center gap-2 p-2.5 rounded-lg bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-700/50"
                    >
                      <span className="text-xs font-mono font-bold text-blue-500 bg-blue-100 dark:bg-blue-500/10 px-1.5 py-0.5 rounded">
                        {clause.clause}
                      </span>
                      <span className="text-xs text-gray-500 truncate">{clause.description}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Validation Info */}
            {query.validation && (
              <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-700/50">
                <div className="flex items-center gap-2 mb-2">
                  <Shield className="w-4 h-4 text-green-500" />
                  <span className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Validation</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${
                    query.validation.syntax
                      ? 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400'
                      : 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                  }`}>
                    {query.validation.syntax ? '✅ Pass' : '❌ Fail'}
                  </span>
                  {query.validation.warnings?.length > 0 && (
                    <span className="text-xs text-yellow-600 dark:text-yellow-400">
                      ⚠ {query.validation.warnings.length} warning(s)
                    </span>
                  )}
                </div>
                {query.validation.warnings?.length > 0 && (
                  <ul className="mt-2 space-y-1">
                    {query.validation.warnings.map((w, i) => (
                      <li key={i} className="text-xs text-yellow-600 dark:text-yellow-400 flex items-center gap-1">
                        <span>•</span> {w}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </motion.div>
        )}
      </div>
    </motion.div>
  );
}

function Database(props) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <ellipse cx="12" cy="5" rx="9" ry="3" />
      <path d="M3 5v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5" />
      <path d="M3 12a2 2 0 0 0 2-2h14a2 2 0 0 0 2-2" />
    </svg>
  );
}