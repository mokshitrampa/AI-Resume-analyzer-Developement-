import api from './api';

export const resumeService = {
  uploadResume: async (formData) => {
    const res = await api.post('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },

  getAllResumes: async () => {
    const res = await api.get('/resumes');
    return res.data;
  },

  getResumeById: async (id) => {
    const res = await api.get(`/resumes/${id}`);
    return res.data;
  },

  deleteResume: async (id) => {
    const res = await api.delete(`/resumes/${id}`);
    return res.data;
  },

  getExtractedData: async (id) => {
    const res = await api.get(`/resumes/${id}/extracted-data`);
    return res.data;
  },

  updateExtractedData: async (id, updatedData) => {
    const res = await api.put(`/resumes/${id}/extracted-data`, updatedData);
    return res.data;
  },

  analyzeResume: async (resumeId) => {
    const res = await api.post(`/analysis/resume/${resumeId}`);
    return res.data;
  },

  getAnalysisById: async (id) => {
    const res = await api.get(`/analysis/${id}`);
    return res.data;
  },

  getAnalysisHistory: async () => {
    const res = await api.get('/analysis/history');
    return res.data;
  },

  getAiImprovementSuggestions: async (resumeId) => {
    const res = await api.get(`/analysis/resume/${resumeId}/improvements`);
    return res.data;
  },
};
