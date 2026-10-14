import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import StatCard from '../../components/StatCard';
import ScoreRing from '../../components/ScoreRing';
import SkillBadge from '../../components/SkillBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import { studentService } from '../../services/studentService';
import {
  FileText,
  Upload,
  Briefcase,
  TrendingUp,
  Award,
  ArrowRight,
  Sparkles,
  BookmarkCheck,
  CheckCircle2,
  AlertCircle,
  ExternalLink
} from 'lucide-react';

const StudentDashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const res = await studentService.getDashboardData();
      setData(res);
    } catch (err) {
      console.error(err);
      setError('Failed to load dashboard data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Student Overview">
        <LoadingSpinner text="Fetching your career intelligence metrics..." size="lg" />
      </DashboardLayout>
    );
  }

  const latestAnalysis = data?.latestAnalysis;
  const latestResume = data?.latestResume;
  const detectedSkills = data?.detectedSkills || [];
  const recommendedJobs = data?.recommendedJobs || [];

  return (
    <DashboardLayout
      title={`Welcome back, ${data?.user?.fullName || 'Student'}!`}
      subtitle="Here is your AI-benchmarked resume overview and curated engineering matches."
      action={
        <Link
          to="/student/upload"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-sm transition-all"
        >
          <Upload className="w-4 h-4" />
          Upload New Resume
        </Link>
      }
    >
      {error && (
        <div className="mb-6 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-2">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Top Stats Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          title="Uploaded Resumes"
          value={data?.totalResumes || 0}
          icon={FileText}
          color="blue"
          subtitle="Document versions stored"
        />
        <StatCard
          title="Latest Quality Score"
          value={latestAnalysis ? `${latestAnalysis.overallScore}%` : 'N/A'}
          icon={Award}
          color="purple"
          subtitle={latestAnalysis ? 'Objective rubric score' : 'Upload to analyze'}
        />
        <StatCard
          title="Detected Skills"
          value={detectedSkills.length}
          icon={Sparkles}
          color="emerald"
          subtitle="Extracted technologies"
        />
        <StatCard
          title="Saved Bookmarks"
          value={data?.savedJobsCount || 0}
          icon={BookmarkCheck}
          color="amber"
          subtitle="Opportunities tracked"
        />
      </div>

      {/* Main Grid: Quality Score & Profile Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
        {/* Latest Resume & Quality Card */}
        <div className="lg:col-span-1 bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Resume Quality Status
              </span>
              {latestResume && (
                <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 dark:bg-blue-900/60 text-blue-700 dark:text-blue-300">
                  v{latestResume.version}
                </span>
              )}
            </div>

            {latestAnalysis ? (
              <div className="flex flex-col items-center py-4">
                <ScoreRing
                  score={latestAnalysis.overallScore}
                  size={140}
                  strokeWidth={10}
                  label="Quality Score"
                  sublabel="Estimated"
                />
                <p className="text-xs text-slate-500 text-center mt-4">
                  Deterministic rubric based on completeness, skill breadth, project descriptions, and readability.
                </p>
                <div className="mt-6 w-full pt-4 border-t border-slate-100 dark:border-slate-800 space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-600 dark:text-slate-400">Completeness:</span>
                    <span className="font-semibold text-slate-900 dark:text-white">{latestAnalysis.completenessScore}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600 dark:text-slate-400">Skill Coverage:</span>
                    <span className="font-semibold text-slate-900 dark:text-white">{latestAnalysis.skillCoverageScore}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-600 dark:text-slate-400">Project Quality:</span>
                    <span className="font-semibold text-slate-900 dark:text-white">{latestAnalysis.projectScore}%</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-12">
                <FileText className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
                <h4 className="font-semibold text-slate-800 dark:text-slate-200 text-sm">No Analysis Available</h4>
                <p className="text-xs text-slate-500 mt-1 max-w-xs mx-auto">
                  Upload your resume to receive an automated quality report and job matching score.
                </p>
              </div>
            )}
          </div>

          <div className="pt-4 border-t border-slate-100 dark:border-slate-800">
            {latestResume ? (
              <Link
                to="/student/analyzer"
                className="w-full inline-flex items-center justify-center gap-2 py-2.5 rounded-xl border border-blue-600 text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/40 text-sm font-semibold transition-colors"
              >
                View Detailed Analysis
                <ArrowRight className="w-4 h-4" />
              </Link>
            ) : (
              <Link
                to="/student/upload"
                className="w-full inline-flex items-center justify-center gap-2 py-2.5 rounded-xl bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
              >
                Upload Resume
                <ArrowRight className="w-4 h-4" />
              </Link>
            )}
          </div>
        </div>

        {/* Detected Skills & Quick Gap Preview */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="font-bold text-base text-slate-900 dark:text-white">Detected Technical Profile</h3>
                <p className="text-xs text-slate-500 mt-0.5">Canonical skills extracted directly from your active resume document</p>
              </div>
              <Link to="/student/matcher" className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline">
                Match Against Jobs →
              </Link>
            </div>

            {detectedSkills.length > 0 ? (
              <div className="flex flex-wrap gap-2 py-3">
                {detectedSkills.map((skill) => (
                  <SkillBadge key={skill} name={skill} variant="neutral" />
                ))}
              </div>
            ) : (
              <div className="py-10 text-center text-slate-400 text-sm">
                No skills detected yet. Upload a resume to automatically parse your programming languages, frameworks, and databases.
              </div>
            )}

            {/* Strengths & Suggestions preview */}
            {latestAnalysis?.strengths?.length > 0 && (
              <div className="mt-4 p-4 rounded-2xl bg-emerald-50/60 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/40">
                <span className="text-xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-300 flex items-center gap-1.5 mb-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  Key Strengths
                </span>
                <ul className="space-y-1 text-xs text-slate-600 dark:text-slate-300">
                  {latestAnalysis.strengths.slice(0, 2).map((s, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <span className="text-emerald-500">•</span>
                      <span>{s}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 flex flex-wrap items-center justify-between gap-4">
            <span className="text-xs text-slate-500">
              Need to add or modify extracted skills?
            </span>
            <Link
              to={latestResume ? `/student/review?id=${latestResume.id}` : '/student/upload'}
              className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline"
            >
              Review &amp; Edit Extracted Data
            </Link>
          </div>
        </div>
      </div>

      {/* Recommended Jobs Section */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="font-bold text-lg text-slate-900 dark:text-white">Recommended Engineering Roles</h3>
            <p className="text-xs text-slate-500 mt-0.5">Matched from live database listings using TF-IDF &amp; skill overlap</p>
          </div>
          <Link to="/student/recommendations" className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline">
            View All ({recommendedJobs.length}) →
          </Link>
        </div>

        {recommendedJobs.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {recommendedJobs.map((job) => (
              <div
                key={job.id}
                className="p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-950/40 hover:border-blue-300 dark:hover:border-blue-700 transition-all flex flex-col justify-between group"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <h4 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-blue-600 transition-colors">
                      {job.title}
                    </h4>
                    {job.matchScore !== null && (
                      <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300">
                        {job.matchScore}%
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 font-medium mb-3">{job.companyName} • {job.location}</p>

                  <div className="flex flex-wrap gap-1 mb-4">
                    {job.requiredSkills.slice(0, 3).map((s) => (
                      <span key={s} className="px-2 py-0.5 rounded-md bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-[11px] text-slate-600 dark:text-slate-300">
                        {s}
                      </span>
                    ))}
                    {job.requiredSkills.length > 3 && (
                      <span className="text-[11px] text-slate-400 px-1">+{job.requiredSkills.length - 3}</span>
                    )}
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-200/60 dark:border-slate-800 flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-700 dark:text-slate-300">{job.salaryRange || 'Competitive'}</span>
                  <Link
                    to={`/student/matcher?jobId=${job.id}`}
                    className="font-medium text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
                  >
                    Match <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-8 text-center text-slate-400 text-sm">
            Upload your resume to automatically generate personalized job recommendations.
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default StudentDashboard;
