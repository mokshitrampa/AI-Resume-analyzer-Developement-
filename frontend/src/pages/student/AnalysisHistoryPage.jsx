import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import { resumeService } from '../../services/resumeService';
import {
  History,
  Award,
  Calendar,
  FileText,
  ArrowRight,
  TrendingUp,
  CheckCircle2
} from 'lucide-react';

const AnalysisHistoryPage = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      setLoading(true);
      const list = await resumeService.getAnalysisHistory();
      setHistory(list);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Analysis History">
        <LoadingSpinner text="Retrieving historical analysis audits..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Analysis History &amp; Progress"
      subtitle="Track your resume quality score improvements over time across versions and iterations."
    >
      {history.length > 0 ? (
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-xs font-semibold uppercase tracking-wider text-slate-500 border-b border-slate-200 dark:border-slate-800">
                <tr>
                  <th className="px-6 py-4">Evaluation Date</th>
                  <th className="px-6 py-4">Quality Score</th>
                  <th className="px-6 py-4">Completeness</th>
                  <th className="px-6 py-4">Skill Coverage</th>
                  <th className="px-6 py-4">Projects</th>
                  <th className="px-6 py-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                {history.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="px-6 py-4 text-slate-700 dark:text-slate-300 font-medium">
                      <div className="flex items-center gap-2">
                        <Calendar className="w-4 h-4 text-slate-400" />
                        {new Date(item.analyzedAt).toLocaleString()}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-blue-100 dark:bg-blue-900/60 text-blue-700 dark:text-blue-300">
                        <Award className="w-3.5 h-3.5" />
                        {item.overallScore}%
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">
                      {item.completenessScore}%
                    </td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">
                      {item.skillCoverageScore}%
                    </td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">
                      {item.projectScore}%
                    </td>
                    <td className="px-6 py-4">
                      <Link
                        to={`/student/analyzer?id=${item.resumeId}`}
                        className="inline-flex items-center gap-1 text-xs font-semibold text-blue-600 hover:underline"
                      >
                        Inspect Breakdown <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800 shadow-sm max-w-lg mx-auto">
          <History className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
          <h4 className="font-bold text-base text-slate-900 dark:text-white">No Analysis Records</h4>
          <p className="text-xs text-slate-500 mt-1 mb-6">
            Upload your resume or run an analysis to begin tracking your quality progress here.
          </p>
          <Link
            to="/student/upload"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 text-white font-semibold text-xs hover:bg-blue-700 transition-colors"
          >
            Upload Resume
          </Link>
        </div>
      )}
    </DashboardLayout>
  );
};

export default AnalysisHistoryPage;
