import api from './api';

export const matchingService = {
  matchResumeWithJob: async (resumeId, jobId) => {
    const res = await api.post('/matching/analyze', { resumeId, jobId });
    return res.data;
  },

  matchResumeWithCustomJobText: async (resumeId, jobTitle, jobDescription, requiredSkills, preferredSkills) => {
    const res = await api.post('/matching/custom', {
      resumeId,
      jobTitle,
      jobDescription,
      requiredSkills,
      preferredSkills,
    });
    return res.data;
  },

  getSkillGapAnalysis: async (resumeId, jobId) => {
    const res = await api.get('/matching/skill-gap', {
      params: { resumeId, jobId },
    });
    return res.data;
  },

  getJobRecommendations: async (resumeId) => {
    const res = await api.get('/matching/recommendations', {
      params: { resumeId },
    });
    return res.data;
  },

  getMatchingHistory: async () => {
    const res = await api.get('/matching/history');
    return res.data;
  },
};
