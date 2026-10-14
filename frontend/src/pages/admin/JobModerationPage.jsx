import React, { useState, useEffect } from 'react';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import { adminService } from '../../services/adminService';
import {
  Briefcase,
  Search,
  CheckCircle2,
  AlertCircle,
  Trash2,
  MapPin,
  Building,
  Calendar,
  DollarSign
} from 'lucide-react';

const JobModerationPage = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadJobs();
  }, []);

  const loadJobs = async () => {
    try {
      setLoading(true);
      const data = await adminService.getAllJobs();
      setJobs(data || []);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch platform job listings.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteJob = async (job) => {
    if (!window.confirm(`Are you sure you want to remove job "${job.title}" at "${job.companyName}"? This will prune all candidate submissions associated with it.`)) {
      return;
    }
    try {
      await adminService.deleteJob(job.id);
      setMessage(`Job "${job.title}" successfully deleted.`);
      setJobs(jobs.filter((j) => j.id !== job.id));
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to delete job listing.');
    }
  };

  const filteredJobs = jobs.filter((j) => {
    const q = search.toLowerCase();
    return (
      j.title?.toLowerCase().includes(q) ||
      j.companyName?.toLowerCase().includes(q) ||
      j.location?.toLowerCase().includes(q) ||
      j.requiredSkills?.some((s) => s.toLowerCase().includes(q))
    );
  });

  return (
    <DashboardLayout
      title="Job Moderation Console"
      subtitle="Audit, inspect, and moderate corporate job vacancies across the platform to ensure recruitment quality."
    >
      <div className="space-y-6">
        {message && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-sm flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600" />
            <span>{message}</span>
          </div>
        )}

        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-2">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-600" />
            <span>{error}</span>
          </div>
        )}

        {/* Filter and Search */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-4 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by title, company, skill, or city..."
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <span className="text-xs text-slate-500 font-medium">
            Showing {filteredJobs.length} of {jobs.length} total listings
          </span>
        </div>

        {/* Jobs List */}
        {loading ? (
          <LoadingSpinner text="Retrieving platform vacancy listings..." size="md" />
        ) : filteredJobs.length > 0 ? (
          <div className="grid grid-cols-1 gap-4">
            {filteredJobs.map((job) => (
              <div
                key={job.id}
                className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col lg:flex-row lg:items-center justify-between gap-6 hover:border-slate-300 dark:hover:border-slate-700 transition-all"
              >
                <div className="space-y-2 max-w-2xl">
                  <div className="flex flex-wrap items-center gap-2.5">
                    <h3 className="font-bold text-base text-slate-900 dark:text-white">{job.title}</h3>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
                      Active
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-800">
                      {job.workMode}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-purple-50 dark:bg-purple-950/50 text-purple-600 dark:text-purple-400 border border-purple-200 dark:border-purple-800">
                      {job.employmentType}
                    </span>
                  </div>

                  <p className="text-xs text-slate-500 flex flex-wrap items-center gap-4">
                    <span className="font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-1">
                      <Building className="w-3.5 h-3.5 text-slate-400" />
                      {job.companyName}
                    </span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {job.location}
                    </span>
                    <span>•</span>
                    <span>Min {job.minExperienceYears || 0} yrs exp</span>
                    {job.salaryRange && (
                      <>
                        <span>•</span>
                        <span className="text-emerald-600 dark:text-emerald-400 font-medium">
                          {job.salaryRange}
                        </span>
                      </>
                    )}
                  </p>

                  <div className="flex flex-wrap gap-1.5 pt-1">
                    <span className="text-[11px] font-semibold text-slate-400 mr-1 self-center">Required:</span>
                    {job.requiredSkills?.map((skill) => (
                      <span
                        key={skill}
                        className="px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-[11px] font-medium"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() => handleDeleteJob(job)}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-rose-50 dark:bg-rose-950/40 hover:bg-rose-100 dark:hover:bg-rose-900/40 text-rose-600 text-xs font-semibold transition-colors cursor-pointer"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    Moderate / Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800">
            <Briefcase className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
            <h4 className="font-bold text-slate-800 dark:text-slate-200 mb-1">
              No matching job listings found
            </h4>
            <p className="text-xs text-slate-500">Try modifying your search or filter keywords.</p>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default JobModerationPage;
