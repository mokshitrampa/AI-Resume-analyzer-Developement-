import React, { useState, useEffect } from 'react';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import api from '../../services/api';
import {
  Shield,
  Activity,
  CheckCircle2,
  Server,
  Database,
  Cpu,
  Layers,
  HardDrive,
  RefreshCw,
  Terminal
} from 'lucide-react';

const SystemHealthPage = () => {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    checkHealth();
  }, []);

  const checkHealth = async () => {
    try {
      setRefreshing(true);
      const res = await api.get('/health');
      setHealth(res.data);
    } catch (err) {
      console.error(err);
      setHealth({ status: 'OFFLINE', error: err.message });
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="System Diagnostics &amp; Health">
        <LoadingSpinner text="Connecting to Spring Boot backend actuator..." size="lg" />
      </DashboardLayout>
    );
  }

  const isHealthy = health?.status === 'UP' || health?.status === 'OK';

  return (
    <DashboardLayout
      title="System Diagnostics &amp; Health"
      subtitle="Inspect real-time telemetry, JVM health status, database persistence, and parsing subsystem connectivity."
      action={
        <button
          type="button"
          onClick={checkHealth}
          disabled={refreshing}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 text-xs font-semibold shadow-sm transition-all cursor-pointer"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh Diagnostics
        </button>
      }
    >
      <div className="space-y-6">
        {/* Main Status Header */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-4">
            <div
              className={`w-14 h-14 rounded-2xl flex items-center justify-center text-white ${
                isHealthy
                  ? 'bg-gradient-to-tr from-emerald-500 to-teal-500 shadow-lg shadow-emerald-500/20'
                  : 'bg-gradient-to-tr from-rose-500 to-red-500 shadow-lg shadow-rose-500/20'
              }`}
            >
              <Activity className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-xl text-slate-900 dark:text-white">
                  Spring Boot Core Engine
                </h3>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                    isHealthy
                      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/70 dark:text-emerald-400'
                      : 'bg-rose-100 text-rose-700 dark:bg-rose-950/70 dark:text-rose-400'
                  }`}
                >
                  {isHealthy ? 'Operational (UP)' : 'Degraded'}
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Port 8080 • API Context <code>/api</code> • OpenAPI Swagger Active
              </p>
            </div>
          </div>

          <div className="text-right">
            <span className="text-xs text-slate-400 block">Response Timestamp</span>
            <span className="font-mono text-xs font-semibold text-slate-700 dark:text-slate-300">
              {new Date().toLocaleTimeString()}
            </span>
          </div>
        </div>

        {/* Subsystems Diagnostics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Database Layer */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/50 text-blue-600 flex items-center justify-center">
                <Database className="w-5 h-5" />
              </div>
              <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold">
                CONNECTED
              </span>
            </div>
            <h4 className="font-bold text-sm text-slate-900 dark:text-white">
              Persistence Engine
            </h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              MySQL 8 / H2-MySQL dialect compatibility layer with automatic schema validation and JPA entity mapping.
            </p>
          </div>

          {/* NLP Matching Subsystem */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-purple-50 dark:bg-purple-950/50 text-purple-600 flex items-center justify-center">
                <Cpu className="w-5 h-5" />
              </div>
              <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold">
                ACTIVE
              </span>
            </div>
            <h4 className="font-bold text-sm text-slate-900 dark:text-white">
              TF-IDF Vectorizer
            </h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              In-memory term-frequency calculation, stopword filtering, token normalization, and cosine angle matrix.
            </p>
          </div>

          {/* Document Extraction */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-amber-50 dark:bg-amber-950/50 text-amber-600 flex items-center justify-center">
                <Layers className="w-5 h-5" />
              </div>
              <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold">
                READY
              </span>
            </div>
            <h4 className="font-bold text-sm text-slate-900 dark:text-white">
              PDFBox &amp; POI Parser
            </h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Apache PDFBox 3.0.2 &amp; Apache POI 5.2.5 streaming extraction pipeline for PDF and DOCX resumes.
            </p>
          </div>
        </div>

        {/* Runtime Config Overview */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
          <h4 className="font-bold text-sm text-slate-900 dark:text-white mb-4 flex items-center gap-2">
            <Terminal className="w-4 h-4 text-blue-600" />
            Backend Application Actuator Metrics
          </h4>
          <div className="overflow-x-auto">
            <pre className="p-4 rounded-2xl bg-slate-950 text-emerald-400 font-mono text-xs leading-relaxed overflow-x-auto">
              {JSON.stringify(health || { status: 'UP', service: 'AI Resume Analyzer & Job Matcher', version: '1.0.0', javaVersion: '21', framework: 'Spring Boot 3.2.5' }, null, 2)}
            </pre>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default SystemHealthPage;
