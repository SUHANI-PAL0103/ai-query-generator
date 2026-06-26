import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useApp } from '../context/AppContext';
import { queryService } from '../services/api';
import QueryCard from '../components/QueryCard';
import ExplanationPanel from '../components/ExplanationPanel';
import ImpactAnalysis from '../components/ImpactAnalysis';
import ValidationPanel from '../components/ValidationPanel';
import ResultTable from '../components/ResultTable';
import {
  Sparkles,
  Eraser,
  Loader2,
  MessageSquare,
  Database,
  X,
  ArrowLeft,
  List,
  Columns,
  Terminal,
  ChevronRight,
  History,
  BookOpen,
} from 'lucide-react';

export default function QueryGenerator() {
  const { state, dispatch } = useApp();
  const [input, setInput] = useState('');
  const [generatedQueries, setGeneratedQueries] = useState([]);
  const [selectedQuery, setSelectedQuery] = useState(null);
  const [executionResults, setExecutionResults] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showComparison, setShowComparison] = useState(false);
  const [step, setStep] = useState('input');

  const handleGenerate = async () => {
    if (!input.trim()) return;
    setIsLoading(true);
    setGeneratedQueries([]);
    setSelectedQuery(null);
    setExecutionResults(null);
    setError('');
    setStep('results');

    try {
      const response = await queryService.generateQuery(input);
      if (!response.success) {
        setError(response.error || 'Failed to generate query');
        setStep('input');
        return;
      }
      setGeneratedQueries(response.data);

      dispatch({
        type: 'ADD_TO_HISTORY',
        payload: {
          id: Date.now(),
          prompt: input,
          queries: response.data,
          timestamp: new Date().toISOString(),
          status: 'generated',
        },
      });
    } catch (error) {
      console.error('Failed to generate query:', error);
      setError(error.message || 'Failed to generate query');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setInput('');
    setGeneratedQueries([]);
    setSelectedQuery(null);
    setExecutionResults(null);
    setError('');
    setStep('input');
  };

  const handleSelect = (query) => {
    setSelectedQuery(query);
    dispatch({ type: 'SET_SELECTED_QUERY', payload: query });
  };

  const handleExecute = async (query) => {
    setIsLoading(true);
    setExecutionResults(null);
    setError('');
    setStep('execution');

    try {
      const response = await queryService.executeQuery(query.sql);
      if (!response.success) {
        setError(response.error || 'Failed to execute query');
        setStep('results');
        return;
      }
      // SELECT queries return data array at response.data
      // DDL/DML queries return the result object at response level
      setExecutionResults(response.data !== undefined ? response.data : response);

      dispatch({
        type: 'ADD_TO_HISTORY',
        payload: {
          id: Date.now(),
          prompt: input,
          query: query.sql,
          timestamp: new Date().toISOString(),
          status: 'executed',
        },
      });
    } catch (error) {
      console.error('Failed to execute query:', error);
      setError(error.message || 'Failed to execute query');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-8 px-4 py-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-800 dark:text-gray-100 tracking-tight">
            Query Generator
          </h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1.5">
            Describe what you need in natural language, and let AI generate the SQL
          </p>
        </div>
        {/* Step Indicator */}
        {step !== 'input' && (
          <div className="hidden sm:flex items-center gap-3 text-xs text-gray-400">
            <span className={`flex items-center gap-1.5 ${step === 'input' || generatedQueries.length > 0 ? 'text-blue-500' : ''}`}>
              <MessageSquare className="w-3.5 h-3.5" /> Prompt
            </span>
            <ChevronRight className="w-3.5 h-3.5" />
            <span className={`flex items-center gap-1.5 ${generatedQueries.length > 0 ? 'text-blue-500' : ''}`}>
              <Terminal className="w-3.5 h-3.5" /> Generate
            </span>
            <ChevronRight className="w-3.5 h-3.5" />
            <span className={`flex items-center gap-1.5 ${executionResults ? 'text-green-500' : ''}`}>
              <Database className="w-3.5 h-3.5" /> Execute
            </span>
          </div>
        )}
      </div>

      {/* Input Section - Clean & Modern */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6 shadow-sm"
      >
        <div className="flex items-center gap-2.5 mb-4">
          <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-500">
            <MessageSquare className="w-4 h-4 text-white" />
          </div>
          <label className="text-sm font-semibold text-gray-700 dark:text-gray-200">
            Natural Language Input
          </label>
          <span className="text-[10px] text-gray-400 bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded-full font-medium ml-auto">
            Describe your query
          </span>
        </div>
        <div className="relative">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Example: Show all employees whose salary is greater than 50000"
            className="w-full h-36 px-5 py-4 bg-gray-50 dark:bg-gray-900 border-2 border-gray-200 dark:border-gray-700 rounded-xl text-sm text-gray-700 dark:text-gray-200 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition-all duration-200 resize-none"
          />
          {input && (
            <button
              onClick={() => setInput('')}
              className="absolute top-3 right-3 p-1.5 rounded-lg bg-gray-200/80 dark:bg-gray-700/80 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-all"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
        <div className="flex items-center gap-3 mt-5">
          <button
            onClick={handleGenerate}
            disabled={!input.trim() || isLoading}
            className="flex items-center gap-2.5 px-6 py-3 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 text-white text-sm font-semibold hover:shadow-lg hover:shadow-blue-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-md"
          >
            {isLoading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Sparkles className="w-4 h-4" />
            )}
            {isLoading ? 'Generating with AI...' : 'Generate Query'}
          </button>
          <button
            onClick={handleClear}
            className="flex items-center gap-2 px-5 py-3 rounded-xl bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-all duration-200"
          >
            <Eraser className="w-4 h-4" />
            Clear
          </button>
          {generatedQueries.length > 0 && (
            <button
              onClick={() => setShowComparison(!showComparison)}
              className="flex items-center gap-2 px-5 py-3 rounded-xl bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-all duration-200 ml-auto"
            >
              <Columns className="w-4 h-4" />
              {showComparison ? 'Card View' : 'Compare View'}
            </button>
          )}
        </div>
      </motion.div>

      {/* Loading State */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 text-sm text-red-600 dark:text-red-400">
          {error}
        </div>
      )}

      <AnimatePresence>
        {isLoading && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="flex flex-col items-center justify-center py-20"
          >
            <div className="relative">
              <div className="w-20 h-20 border-4 border-blue-200 dark:border-blue-900 rounded-full border-t-blue-500 animate-spin" />
              <Sparkles className="w-8 h-8 text-blue-500 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" />
            </div>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-6 font-medium">
              {step === 'execution'
                ? 'Executing query against database...'
                : 'Generating SQL query with AI...'}
            </p>
            <p className="text-xs text-gray-400 mt-1.5">
              {step === 'execution'
                ? 'This may take a moment depending on data size'
                : 'Processing your request through HuggingFace AI'}
            </p>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Generated Results */}
      {!isLoading && generatedQueries.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-8"
        >
          {/* Section Header */}
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500">
              <Terminal className="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-800 dark:text-gray-100">
                Generated Queries
              </h2>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                {generatedQueries.length} query{generatedQueries.length > 1 ? 'ies' : 'y'} generated from your prompt
              </p>
            </div>
          </div>

          {/* Comparison View */}
          {showComparison ? (
            <div className="rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 overflow-hidden shadow-sm">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-gray-50 dark:bg-gray-800/80">
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Query</th>
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Complexity</th>
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Cost</th>
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Rows</th>
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Risk</th>
                      <th className="text-left px-5 py-4 font-semibold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 dark:divide-gray-700/50">
                    {generatedQueries.map((q, i) => (
                      <tr
                        key={q.id}
                        className={`hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors ${
                          selectedQuery?.id === q.id
                            ? 'bg-blue-50 dark:bg-blue-500/5'
                            : ''
                        }`}
                      >
                        <td className="px-5 py-4">
                          <code className="text-xs bg-gray-100 dark:bg-gray-700 px-2.5 py-1.5 rounded text-gray-600 dark:text-gray-300 font-mono max-w-[250px] truncate block border border-gray-200 dark:border-gray-600">
                            {q.sql.split('\n')[0]}
                          </code>
                        </td>
                        <td className="px-5 py-4 text-xs text-gray-500">{q.complexity}</td>
                        <td className="px-5 py-4 text-xs text-gray-500">{q.cost}</td>
                        <td className="px-5 py-4 text-xs text-gray-500">{q.rowsReturned !== undefined ? q.rowsReturned : 'N/A'}</td>
                        <td className="px-5 py-4">
                          <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            q.risk === 'Low'
                              ? 'bg-green-100 dark:bg-green-500/10 text-green-600 dark:text-green-400'
                              : q.risk === 'Medium'
                                ? 'bg-yellow-100 dark:bg-yellow-500/10 text-yellow-600 dark:text-yellow-400'
                                : 'bg-red-100 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                          }`}>
                            {q.risk}
                          </span>
                        </td>
                        <td className="px-5 py-4">
                          <button
                            onClick={() => handleSelect(q)}
                            className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                              selectedQuery?.id === q.id
                                ? 'bg-blue-500 text-white shadow-sm'
                                : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                            }`}
                          >
                            {selectedQuery?.id === q.id ? 'Selected' : 'Select'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            /* Single Column Layout for Full Visibility */
            <div className="space-y-6">
              {generatedQueries.map((q) => (
                <QueryCard
                  key={q.id}
                  query={q}
                  onSelect={handleSelect}
                  onExecute={handleExecute}
                  isSelected={selectedQuery?.id === q.id}
                />
              ))}
            </div>
          )}

          {/* Selected Query Details - Full Width Panels */}
          {selectedQuery && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="space-y-6"
            >
              <div className="flex items-center gap-3">
                <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-green-500 to-emerald-500">
                  <BookOpen className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-gray-800 dark:text-gray-100">
                    Query Details & Analysis
                  </h2>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    Detailed explanation, impact analysis, and validation
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <ExplanationPanel query={selectedQuery} />
                <ImpactAnalysis query={selectedQuery} />
              </div>
              <div>
                <ValidationPanel query={selectedQuery} />
              </div>
            </motion.div>
          )}
        </motion.div>
      )}

      {/* Execution Results - handles both SELECT row data and DDL success messages */}
      {executionResults && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-4"
        >
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-green-500 to-emerald-500">
              <Database className="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-800 dark:text-gray-100">
                Execution Results
              </h2>
              {Array.isArray(executionResults) ? (
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  {executionResults.length} row(s) returned from your query
                </p>
              ) : (
                <p className="text-xs text-green-500 dark:text-green-400 font-medium">
                  ✓ {executionResults.message || 'Statement executed successfully'}
                </p>
              )}
            </div>
          </div>
          {Array.isArray(executionResults) ? (
            <ResultTable results={executionResults} />
          ) : (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="rounded-2xl border border-green-200 dark:border-green-700 bg-green-50 dark:bg-green-500/5 p-6 text-center"
            >
              <div className="flex items-center justify-center gap-3">
                <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-500/10 flex items-center justify-center">
                  <Database className="w-6 h-6 text-green-500" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-semibold text-green-700 dark:text-green-300">
                    {executionResults.message || 'Statement executed successfully'}
                  </p>
                  {executionResults.affectedRows !== undefined && (
                    <p className="text-xs text-green-600 dark:text-green-400 mt-0.5">
                      Affected rows: {executionResults.affectedRows}
                    </p>
                  )}
                  <p className="text-xs text-green-500 dark:text-green-400 mt-0.5">
                    The database has been updated. You can verify by checking the schema.
                  </p>
                </div>
              </div>
            </motion.div>
          )}
        </motion.div>
      )}

      {/* No Results Yet - Empty State */}
      {!isLoading && generatedQueries.length === 0 && !executionResults && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center justify-center py-16 text-center"
        >
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-600 flex items-center justify-center mb-5">
            <Terminal className="w-7 h-7 text-gray-400" />
          </div>
          <h3 className="text-base font-semibold text-gray-700 dark:text-gray-300 mb-2">
            Ready to Generate SQL
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md">
            Type your request in natural language above and click "Generate Query" to convert it into SQL using AI.
          </p>
        </motion.div>
      )}
    </div>
  );
}
