import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import StatCard from '../../components/StatCard';
import LoadingSpinner from '../../components/LoadingSpinner';
import { recruiterService } from '../../services/recruiterService';
import {
  Briefcase,
  Users,
  CheckCircle2,
  PlusCircle,
  ArrowRight,
  TrendingUp,
  MapPin,
  Calendar
} from 'lucide-react';

const RecruiterDashboard = () => {
  const [stats, setStats] = useState(null);
  const [myJobs, setMyJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRecruiterData();
  }, []);

  const loadRecruiterData = async () => {
    try {
      setLoading(true);
      const [statsData, jobsList] = await Promise.all([
        recruiterService.getDashboardStats(),
        recruiterService.getMyJobs(),
      ]);
      setStats(statsData);
      setMyJobs(jobsList);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Recruiter Dashboard">
        <LoadingSpinner text="Loading your recruiting intelligence..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Recruiter Management Hub"
      subtitle="Publish engineering positions, evaluate candidate match scores, and shortlist candidates."
      action={
        <Link
          to="/recruiter/post-job"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-sm transition-all"
        >
          <PlusCircle className="w-4 h-4" />
          Post New Vacancy
        </Link>
      }
    >
      <div className="space-y-8">
        {/* Metric Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Job Postings"
            value={stats?.totalJobs || 0}
            icon={Briefcase}
            color="blue"
            subtitle="Positions published"
          />
          <StatCard
            title="Active Listings"
            value={stats?.activeJobs || 0}
            icon={TrendingUp}
            color="emerald"
            subtitle="Open for candidate applications"
          />
          <StatCard
            title="Total Applicants"
            value={stats?.totalApplications || 0}
            icon={Users}
            color="purple"
            subtitle="Candidate submissions"
          />
          <StatCard
            title="Shortlisted"
            value={stats?.shortlistedCount || 0}
            icon={CheckCircle2}
            color="amber"
            subtitle="Selected for interviews"
          />
        </div>

        {/* Recent Jobs */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="font-bold text-lg text-slate-900 dark:text-white">Your Listed Jobs</h3>
              <p className="text-xs text-slate-500 mt-0.5">Manage positions and review candidate match metrics</p>
            </div>
            <Link to="/recruiter/manage-jobs" className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline">
              View All ({myJobs.length}) →
            </Link>
          </div>

          {myJobs.length > 0 ? (
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {myJobs.slice(0, 5).map((job) => (
                <div key={job.id} className="py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                  <div>
                    <h4 className="font-bold text-sm text-slate-900 dark:text-white">{job.title}</h4>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {job.companyName} • {job.location} • <span className="font-medium text-slate-600 dark:text-slate-400">{job.workMode}</span>
                    </p>
                    <div className="flex flex-wrap gap-1 mt-2">
                      {job.requiredSkills?.slice(0, 4).map((s) => (
                        <span key={s} className="px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-[10px] text-slate-600 dark:text-slate-300 font-medium">
                          {s}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <Link
                      to={`/recruiter/applications?jobId=${job.id}`}
                      className="px-4 py-2 rounded-xl bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-300 hover:bg-blue-100 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                    >
                      <Users className="w-3.5 h-3.5" />
                      Review Applicants
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center text-slate-400 text-sm">
              You haven't posted any jobs yet. Click "Post New Vacancy" to publish your first engineering position.
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default RecruiterDashboard;
