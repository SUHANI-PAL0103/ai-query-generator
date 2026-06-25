import { motion } from 'framer-motion';
import { CheckCircle, XCircle, AlertTriangle, Lightbulb } from 'lucide-react';

export default function ValidationPanel({ query }) {
  if (!query?.validation) return null;

  const { validation } = query;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
    >
      <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
        <CheckCircle className="w-4 h-4 text-green-500" />
        Validation & Optimization
      </h3>

      {/* Syntax Status */}
      <div
        className={`flex items-center gap-3 p-4 rounded-xl mb-4 ${
          validation.syntax
            ? 'bg-green-50 dark:bg-green-500/5 border border-green-200 dark:border-green-500/20'
            : 'bg-red-50 dark:bg-red-500/5 border border-red-200 dark:border-red-500/20'
        }`}
      >
        {validation.syntax ? (
          <CheckCircle className="w-5 h-5 text-green-500 shrink-0" />
        ) : (
          <XCircle className="w-5 h-5 text-red-500 shrink-0" />
        )}
        <div>
          <p
            className={`text-sm font-medium ${
              validation.syntax
                ? 'text-green-700 dark:text-green-400'
                : 'text-red-700 dark:text-red-400'
            }`}
          >
            {validation.syntax ? 'Query Syntax Valid' : 'Syntax Error Detected'}
          </p>
          <p className="text-xs text-gray-500 mt-0.5">
            {validation.syntax
              ? 'The SQL query is syntactically correct.'
              : 'Please fix the syntax errors before executing.'}
          </p>
        </div>
      </div>

      {/* Warnings */}
      {validation.warnings?.length > 0 && (
        <div className="mb-4">
          <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 flex items-center gap-1">
            <AlertTriangle className="w-3.5 h-3.5 text-yellow-500" />
            Warnings
          </h4>
          <div className="space-y-2">
            {validation.warnings.map((warning, i) => (
              <div
                key={i}
                className="flex items-start gap-2 p-3 rounded-xl bg-yellow-50 dark:bg-yellow-500/5 border border-yellow-200 dark:border-yellow-500/20"
              >
                <AlertTriangle className="w-4 h-4 text-yellow-500 shrink-0 mt-0.5" />
                <p className="text-sm text-yellow-700 dark:text-yellow-400">
                  {warning}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Suggestions */}
      {validation.suggestions?.length > 0 && (
        <div>
          <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 flex items-center gap-1">
            <Lightbulb className="w-3.5 h-3.5 text-blue-500" />
            Optimization Suggestions
          </h4>
          <div className="space-y-2">
            {validation.suggestions.map((suggestion, i) => (
              <div
                key={i}
                className="flex items-start gap-2 p-3 rounded-xl bg-blue-50 dark:bg-blue-500/5 border border-blue-200 dark:border-blue-500/20"
              >
                <Lightbulb className="w-4 h-4 text-blue-500 shrink-0 mt-0.5" />
                <p className="text-sm text-blue-700 dark:text-blue-400">
                  {suggestion}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </motion.div>
  );
}