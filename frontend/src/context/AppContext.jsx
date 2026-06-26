import { createContext, useContext, useReducer, useEffect } from 'react';
import { queryService } from '../services/api';

const AppContext = createContext();

const initialState = {
  theme: localStorage.getItem('theme') || 'dark',
  sidebarOpen: true,
  queries: [],
  queryHistory: JSON.parse(localStorage.getItem('queryHistory') || '[]'),
  selectedQuery: null,
  executionResults: null,
  // Will be populated from real DB schema (or left empty if not loaded)
  databaseSchema: [],

  generatedQueries: [],
  isLoading: false,
  isAdmin: false,
  dbConnection: JSON.parse(localStorage.getItem('dbConnection') || 'null'),
};

function appReducer(state, action) {
  switch (action.type) {
    case 'SET_DATABASE_SCHEMA':
      return { ...state, databaseSchema: action.payload };

    case 'TOGGLE_SIDEBAR':
      return { ...state, sidebarOpen: !state.sidebarOpen };
    case 'SET_THEME':
      localStorage.setItem('theme', action.payload);
      return { ...state, theme: action.payload };
    case 'ADD_QUERY':
      return { ...state, queries: [...state.queries, action.payload] };
    case 'ADD_TO_HISTORY': {
      const history = [action.payload, ...state.queryHistory].slice(0, 50);
      localStorage.setItem('queryHistory', JSON.stringify(history));
      return { ...state, queryHistory: history };
    }
    case 'SET_QUERY_HISTORY':
      return { ...state, queryHistory: action.payload };
    case 'SET_GENERATED_QUERIES':
      return { ...state, generatedQueries: action.payload };
    case 'SET_SELECTED_QUERY':
      return { ...state, selectedQuery: action.payload };
    case 'SET_EXECUTION_RESULTS':
      return { ...state, executionResults: action.payload };
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'DELETE_HISTORY_ITEM': {
      const filtered = state.queryHistory.filter((_, i) => i !== action.payload);
      localStorage.setItem('queryHistory', JSON.stringify(filtered));
      return { ...state, queryHistory: filtered };
    }
    case 'CLEAR_HISTORY': {
      localStorage.setItem('queryHistory', '[]');
      return { ...state, queryHistory: [] };
    }
    case 'SET_IS_ADMIN':
      return { ...state, isAdmin: action.payload };
    case 'SET_DB_CONNECTION': {
      localStorage.setItem('dbConnection', JSON.stringify(action.payload));
      return { ...state, dbConnection: action.payload };
    }
    default:
      return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState);

  useEffect(() => {
    const loadSchema = async () => {
      const conn = state.dbConnection;
      if (!conn || !conn.host || !conn.username || !conn.password) {
        // User hasn't connected their database yet; skip schema fetch
        return;
      }
      try {
        const result = await queryService.getDatabaseSchema();
        if (result.success) {
          dispatch({ type: 'SET_DATABASE_SCHEMA', payload: result.data });
        }
      } catch (e) {
        console.warn('Could not load database schema from backend:', e);
      }
    };
    loadSchema();
  }, [state.dbConnection]);

  useEffect(() => {
    const loadHistory = async () => {
      const conn = state.dbConnection;
      if (!conn || !conn.host || !conn.username || !conn.password) return;
      try {
        const result = await queryService.getQueryHistory();
        if (result.success && Array.isArray(result.data)) {
          dispatch({ type: 'SET_QUERY_HISTORY', payload: result.data });
        }
      } catch (e) {
        console.warn('Could not load query history from backend:', e);
      }
    };
    loadHistory();
  }, [state.dbConnection]);


  useEffect(() => {
    document.documentElement.classList.toggle('dark', state.theme === 'dark');
  }, [state.theme]);

  return (
    <AppContext.Provider value={{ state, dispatch }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
}

export default AppContext;
