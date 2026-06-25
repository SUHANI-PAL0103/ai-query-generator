import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useApp } from '../context/AppContext';
import {
  Database,
  ChevronRight,
  ChevronDown,
  Key,
  Link2,
  Search,
  Table2,
  Columns,
} from 'lucide-react';

function SchemaTree({ table, defaultOpen = false }) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <div className="select-none">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-all duration-200 group"
      >
        {isOpen ? (
          <ChevronDown className="w-4 h-4 text-gray-400" />
        ) : (
          <ChevronRight className="w-4 h-4 text-gray-400" />
        )}
        <Table2 className="w-5 h-5 text-blue-500" />
        <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">
          {table.name}
        </span>
        <span className="text-xs text-gray-400 ml-auto">
          {table.columns.length} columns
        </span>
      </button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="overflow-hidden"
          >
            <div className="ml-8 pl-4 border-l-2 border-gray-200 dark:border-gray-700 space-y-1 py-2">
              {table.columns.map((column, i) => (
                <div
                  key={i}
                  className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors"
                >
                  <Columns className="w-3.5 h-3.5 text-gray-400 shrink-0" />
                  <span className="text-sm text-gray-700 dark:text-gray-300 font-mono">
                    {column.name}
                  </span>
                  <span className="text-xs text-gray-400 font-mono">
                    {column.type}
                  </span>
                  {column.primaryKey && (
                    <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-yellow-100 dark:bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 text-[10px] font-medium">
                      <Key className="w-2.5 h-2.5" />
                      PK
                    </span>
                  )}
                </div>
              ))}

              {/* Foreign Keys */}
              {table.foreignKeys?.map((fk, i) => (
                <div
                  key={`fk-${i}`}
                  className="flex items-center gap-3 px-3 py-2 rounded-lg bg-purple-50 dark:bg-purple-500/5 border border-purple-100 dark:border-purple-500/10"
                >
                  <Link2 className="w-3.5 h-3.5 text-purple-500 shrink-0" />
                  <span className="text-xs text-purple-600 dark:text-purple-400 font-mono">
                    {fk.column} → {fk.references}
                  </span>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default function Schema() {
  const { state } = useApp();
  const { databaseSchema } = state;
  const [searchTerm, setSearchTerm] = useState('');
  const [expandAll, setExpandAll] = useState(false);

  const filteredSchema = databaseSchema.filter((table) =>
    table.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            Database Schema
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            View tables, columns, and relationships
          </p>
        </div>
        <button
          onClick={() => setExpandAll(!expandAll)}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-all duration-200"
        >
          <Database className="w-4 h-4" />
          {expandAll ? 'Collapse All' : 'Expand All'}
        </button>
      </div>

      {/* Search */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search tables..."
          className="w-full pl-10 pr-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-300 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200"
        />
      </div>

      {/* Schema Tree */}
      <div className="space-y-2">
        {filteredSchema.map((table, i) => (
          <motion.div
            key={table.name}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
            className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 overflow-hidden hover:shadow-md transition-all duration-200"
          >
            <SchemaTree table={table} defaultOpen={expandAll} />
          </motion.div>
        ))}
      </div>

      {/* Relationships Overview */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6"
      >
        <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
          <Link2 className="w-4 h-4 text-purple-500" />
          Table Relationships
        </h3>
        <div className="space-y-3">
          {databaseSchema
            .filter((t) => t.foreignKeys?.length > 0)
            .map((table) =>
              table.foreignKeys.map((fk, i) => (
                <div
                  key={`${table.name}-${i}`}
                  className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/80 border border-gray-100 dark:border-gray-700/50"
                >
                  <div className="flex items-center gap-2 text-sm">
                    <span className="font-mono font-semibold text-blue-500">
                      {table.name}
                    </span>
                    <span className="text-gray-400">.</span>
                    <span className="font-mono text-purple-500">
                      {fk.column}
                    </span>
                  </div>
                  <Link2 className="w-4 h-4 text-gray-400 shrink-0" />
                  <div className="flex items-center gap-2 text-sm">
                    <span className="font-mono font-semibold text-blue-500">
                      {fk.references}
                    </span>
                  </div>
                </div>
              ))
            )}
        </div>
      </motion.div>
    </div>
  );
}