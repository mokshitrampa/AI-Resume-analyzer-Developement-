import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import StatCard from '../../components/StatCard';
import LoadingSpinner from '../../components/LoadingSpinner';
import { adminService } from '../../services/adminService';
import {
  Users,
  Briefcase,
  FileSearch,
  BookOpen,
  Shield,
  Activity,
  Server,
  Database,
  ArrowRight,
  TrendingUp,
  Cpu,
  CheckCircle2
} from 'lucide-react';

const AdminDashboard = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      const data = await adminService.getDashboardAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Admin Control Console">
        <LoadingSpinner text="Aggregating platform system statistics..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Platform Administration Console"
      subtitle="Monitor system-wide metrics, oversee registered users, audit job listings, and manage NLP skill dictionaries."
    >
      <div className="space-y-8">
        {/* Metric Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Accounts"
            value={analytics?.totalUsers || 0}
            icon={Users}
            color="blue"
            subtitle={`${analytics?.totalStudents || 0} Students • ${analytics?.totalRecruiters || 0} Recruiters`}
          />
          <StatCard
            title="Resumes Analyzed"
            value={analytics?.totalResumesAnalyzed || 0}
            icon={FileSearch}
            color="emerald"
            subtitle="Processed with TF-IDF"
          />
          <StatCard
            title="Job Listings"
            value={analytics?.totalJobs || 0}
            icon={Briefcase}
            color="purple"
            subtitle={`${analytics?.activeJobs || 0} Active vacancies`}
          />
          <StatCard
            title="Skill Dictionary"
            value={analytics?.totalSkillsInDictionary || 150}
            icon={BookOpen}
            color="amber"
            subtitle="Recognized technical skills"
          />
        </div>

        {/* Quick Operations Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Link
            to="/admin/users"
            className="group bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm hover:border-blue-500 transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-blue-50 dark:bg-blue-950/50 text-blue-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <Users className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base text-slate-900 dark:text-white mb-1">
                User Management
              </h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Inspect registered students, recruiters, and administrators. Enable, disable, or delete accounts.
              </p>
            </div>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-blue-600 dark:text-blue-400">
              <span>Manage Accounts</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>

          <Link
            to="/admin/jobs"
            className="group bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm hover:border-purple-500 transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-purple-50 dark:bg-purple-950/50 text-purple-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <Briefcase className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base text-slate-900 dark:text-white mb-1">
                Job Moderation
              </h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Audit corporate job listings, inspect skill requirements, and prune obsolete or duplicate openings.
              </p>
            </div>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-purple-600 dark:text-purple-400">
              <span>Audit Job Postings</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>

          <Link
            to="/admin/skills"
            className="group bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm hover:border-amber-500 transition-all flex flex-col justify-between"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/50 text-amber-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <BookOpen className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base text-slate-900 dark:text-white mb-1">
                Skill Taxonomy &amp; Synonyms
              </h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Expand the NLP dictionary with new programming languages, frameworks, and acronym aliases (e.g., TS &rarr; TypeScript).
              </p>
            </div>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-amber-600 dark:text-amber-400">
              <span>Edit Skill Dictionary</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>
        </div>

        {/* System Architecture & Health Panel */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 flex items-center justify-center">
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-base text-slate-900 dark:text-white">
                  System Health &amp; Runtime Environment
                </h3>
                <p className="text-xs text-slate-500">Live infrastructure configuration</p>
              </div>
            </div>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              All Services Operational
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
              <span className="text-slate-400 font-medium block mb-1">Backend Runtime</span>
              <span className="font-bold text-slate-900 dark:text-white text-sm">Java 21 LTS</span>
              <span className="text-slate-500 block mt-0.5">Spring Boot 3.2.5</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
              <span className="text-slate-400 font-medium block mb-1">Persistence Layer</span>
              <span className="font-bold text-slate-900 dark:text-white text-sm">MySQL 8 / H2-MySQL</span>
              <span className="text-slate-500 block mt-0.5">Spring Data JPA + Hibernate</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
              <span className="text-slate-400 font-medium block mb-1">NLP Engine</span>
              <span className="font-bold text-slate-900 dark:text-white text-sm">Pure Java TF-IDF</span>
              <span className="text-slate-500 block mt-0.5">Cosine Similarity + Apache PDFBox</span>
            </div>

            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
              <span className="text-slate-400 font-medium block mb-1">Frontend Client</span>
              <span className="font-bold text-slate-900 dark:text-white text-sm">React 19 + Vite</span>
              <span className="text-slate-500 block mt-0.5">Tailwind CSS + Recharts</span>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default AdminDashboard;
