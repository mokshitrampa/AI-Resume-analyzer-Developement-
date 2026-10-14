import api from './api';

export const recruiterService = {
  getDashboardData: async () => {
    const res = await api.get('/recruiter/dashboard');
    return res.data;
  },

  getDashboardStats: async () => {
    const res = await api.get('/recruiter/dashboard');
    return res.data;
  },

  getMyJobs: async () => {
    const res = await api.get('/recruiter/jobs');
    return res.data;
  },

  createJob: async (jobData) => {
    const res = await api.post('/recruiter/jobs', jobData);
    return res.data;
  },

  updateJob: async (jobId, jobData) => {
    const res = await api.put(`/recruiter/jobs/${jobId}`, jobData);
    return res.data;
  },

  deleteJob: async (jobId) => {
    const res = await api.delete(`/recruiter/jobs/${jobId}`);
    return res.data;
  },

  getJobApplications: async (jobId) => {
    const res = await api.get(`/recruiter/jobs/${jobId}/applications`);
    return res.data;
  },

  updateApplicationStatus: async (applicationId, status, notes = '') => {
    const res = await api.put(`/recruiter/applications/${applicationId}/status`, { status, notes });
    return res.data;
  },
};
