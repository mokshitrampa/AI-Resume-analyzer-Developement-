import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import { jobService } from '../../services/jobService';
import {
  BookmarkCheck,
  Briefcase,
  MapPin,
  Trash2,
  ArrowRight,
  ExternalLink
} from 'lucide-react';

const SavedJobsPage = () => {
  const [savedJobs, setSavedJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSavedJobs();
  }, []);

  const loadSavedJobs = async () => {
    try {
      setLoading(true);
      const list = await jobService.getSavedJobs();
      setSavedJobs(list);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (jobId) => {
    try {
      await jobService.unsaveJob(jobId);
      setSavedJobs((prev) => prev.filter((j) => j.id !== jobId));
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Saved Jobs">
        <LoadingSpinner text="Loading your bookmarked jobs..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Saved Jobs &amp; Bookmarks"
      subtitle="Track opportunities you have saved for later review or application."
    >
      {savedJobs.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {savedJobs.map((job) => (
            <div
              key={job.id}
              className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    {job.workMode}
                  </span>
                  <button
                    type="button"
                    onClick={() => handleRemove(job.id)}
                    className="p-1 rounded-lg text-slate-400 hover:text-rose-600 transition-colors"
                    title="Remove from saved"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <h3 className="font-bold text-base text-slate-900 dark:text-white mb-1">{job.title}</h3>
                <p className="text-xs text-slate-500 mb-4">{job.companyName} • {job.location}</p>

                <div className="flex flex-wrap gap-1 mb-4">
                  {job.requiredSkills?.slice(0, 4).map((s) => (
                    <span key={s} className="px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-[11px] text-slate-700 dark:text-slate-300">
                      {s}
                    </span>
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <span className="text-xs font-bold text-slate-800 dark:text-slate-200">{job.salaryRange || 'Competitive'}</span>
                <Link
                  to={`/student/matcher?jobId=${job.id}`}
                  className="inline-flex items-center gap-1 text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline"
                >
                  Analyze Match <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800 shadow-sm max-w-lg mx-auto">
          <BookmarkCheck className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
          <h4 className="font-bold text-base text-slate-900 dark:text-white">No Saved Jobs Yet</h4>
          <p className="text-xs text-slate-500 mt-1 mb-6">
            Browse our curated engineering recommendations and click the bookmark icon to save roles here.
          </p>
          <Link
            to="/student/recommendations"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 text-white font-semibold text-xs hover:bg-blue-700 transition-colors"
          >
            Browse Recommendations
          </Link>
        </div>
      )}
    </DashboardLayout>
  );
};

export default SavedJobsPage;
