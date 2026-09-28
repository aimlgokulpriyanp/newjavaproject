/**
 * GrievanceTrack — Modern Civic Application JavaScript
 * Connects frontend to Spring Boot REST APIs with complete SLA & Escalation workflows
 */

const API_BASE = '/api';

// ================= API SERVICE =================
const API = {
  async get(url, params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    const fullUrl = query.toString() ? `${url}?${query.toString()}` : url;
    const res = await fetch(fullUrl);
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw err;
    }
    return res.status === 204 ? null : await res.json();
  },

  async post(url, data) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw err;
    }
    return await res.json();
  },

  async put(url, data = {}) {
    const res = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw err;
    }
    return res.status === 204 ? null : await res.json().catch(() => null);
  },

  async delete(url) {
    const res = await fetch(url, { method: 'DELETE' });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw err;
    }
    return null;
  }
};

// ================= APPLICATION STATE =================
const State = {
  currentView: 'landing',
  categories: [],
  departments: [],
  notifications: [],
  unreadNotifCount: 0,
  selectedRating: 0,
  myComplaints: {
    page: 0,
    size: 8,
    sort: 'createdAt,desc',
    search: '',
    status: '',
    categoryId: ''
  },
  officerComplaints: {
    page: 0,
    size: 10,
    sort: 'createdAt,desc',
    status: ''
  },
  currentEditingComplaint: null
};

// ================= TOAST SYSTEM =================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <div class="toast-msg">${escapeHtml(message)}</div>
    <button class="close-btn" style="font-size:1.1rem;" onclick="this.parentElement.remove()">&times;</button>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ================= UTILITIES =================
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatDate(isoStr) {
  if (!isoStr) return 'N/A';
  const d = new Date(isoStr);
  return d.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    showToast(`Copied ${text} to clipboard!`, 'success');
  }).catch(() => {
    showToast(`Failed to copy`, 'error');
  });
}

function getStatusBadge(status) {
  const map = {
    PENDING: { label: 'Pending', icon: '⏳', cls: 'badge-pending' },
    ASSIGNED: { label: 'Assigned', icon: '📌', cls: 'badge-assigned' },
    IN_PROGRESS: { label: 'In Progress', icon: '⚙️', cls: 'badge-in_progress' },
    RESOLVED: { label: 'Resolved', icon: '✓', cls: 'badge-resolved' },
    ESCALATED: { label: 'Escalated', icon: '⚠', cls: 'badge-escalated' },
    REJECTED: { label: 'Rejected', icon: '✕', cls: 'badge-rejected' }
  };
  const item = map[status] || { label: status, icon: '•', cls: 'badge-pending' };
  return `<span class="badge ${item.cls}">${item.icon} ${item.label}</span>`;
}

function getSlaBadge(slaStatus) {
  const map = {
    ON_TRACK: { label: 'On Track', icon: '🛡️', cls: 'sla-on_track' },
    DUE_SOON: { label: 'Due Soon', icon: '⏱️', cls: 'sla-due_soon' },
    SLA_BREACHED: { label: 'SLA Breached', icon: '🚨', cls: 'sla-sla_breached' },
    ESCALATED: { label: 'Escalated', icon: '⚡', cls: 'sla-escalated' },
    COMPLETED: { label: 'Completed', icon: '✓', cls: 'sla-completed' }
  };
  const item = map[slaStatus] || { label: slaStatus, icon: '•', cls: 'sla-on_track' };
  return `<span class="sla-badge ${item.cls}">${item.icon} ${item.label}</span>`;
}

// ================= VIEW NAVIGATION =================
function showView(viewId, param = null) {
  document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active'));

  const targetView = document.getElementById(`view-${viewId}`);
  if (targetView) {
    targetView.classList.add('active');
    State.currentView = viewId;
  }

  const activeNav = document.querySelector(`.nav-link[data-view="${viewId}"]`);
  if (activeNav) activeNav.classList.add('active');

  window.scrollTo({ top: 0, behavior: 'smooth' });

  // View-specific initialization
  if (viewId === 'dashboard') {
    loadCitizenDashboard();
  } else if (viewId === 'submit') {
    initSubmitForm();
  } else if (viewId === 'track') {
    if (param) {
      document.getElementById('track-search-input').value = param;
      trackComplaintByCode(param);
    }
  } else if (viewId === 'my-complaints') {
    loadMyComplaints();
  } else if (viewId === 'officer') {
    loadOfficerDashboard();
  } else if (viewId === 'admin') {
    loadAdminDashboard();
  }
}

// ================= LOAD INITIAL METADATA =================
async function loadMetadata() {
  try {
    const [cats, depts] = await Promise.all([
      API.get(`${API_BASE}/categories`),
      API.get(`${API_BASE}/departments`)
    ]);
    State.categories = cats;
    State.departments = depts;

    populateCategoryDropdowns();
    renderLandingCategories();
    updateNotificationCount();
  } catch (err) {
    console.error('Failed to load initial metadata:', err);
  }
}

function populateCategoryDropdowns() {
  const submitCatSelect = document.getElementById('submit-category');
  const filterCatSelect = document.getElementById('filter-category');

  if (submitCatSelect) {
    submitCatSelect.innerHTML = '<option value="">-- Select Category --</option>';
    State.categories.forEach(c => {
      submitCatSelect.innerHTML += `<option value="${c.id}">${escapeHtml(c.name)} (SLA: ${c.slaDays} days)</option>`;
    });
  }

  if (filterCatSelect) {
    filterCatSelect.innerHTML = '<option value="">All Categories</option>';
    State.categories.forEach(c => {
      filterCatSelect.innerHTML += `<option value="${c.id}">${escapeHtml(c.name)}</option>`;
    });
  }
}

function renderLandingCategories() {
  const container = document.getElementById('landing-categories-grid');
  if (!container) return;

  container.innerHTML = State.categories.map(c => `
    <div class="category-card" onclick="selectCategoryAndSubmit(${c.id})">
      <div class="cat-header">
        <span class="cat-title">${escapeHtml(c.name)}</span>
        <span class="sla-chip">${c.slaDays} Day${c.slaDays > 1 ? 's' : ''} SLA</span>
      </div>
      <div class="cat-dept">Auto-assigned: <strong>${escapeHtml(c.departmentName || 'Department')}</strong></div>
      <div style="font-size:0.8rem; color:var(--primary-600); font-weight:600; margin-top:0.75rem;">
        Submit in this category &rarr;
      </div>
    </div>
  `).join('');
}

function selectCategoryAndSubmit(catId) {
  showView('submit');
  const select = document.getElementById('submit-category');
  if (select) {
    select.value = catId;
    select.dispatchEvent(new Event('change'));
  }
}

// ================= SUBMIT COMPLAINT =================
function initSubmitForm() {
  const form = document.getElementById('submit-complaint-form');
  const catSelect = document.getElementById('submit-category');
  const previewBox = document.getElementById('dept-preview-box');
  const descInput = document.getElementById('submit-desc');
  const descCounter = document.getElementById('desc-char-count');

  if (descInput && descCounter) {
    descInput.addEventListener('input', () => {
      descCounter.textContent = `${descInput.value.length} / 10 min characters`;
      if (descInput.value.length >= 10) {
        descCounter.style.color = 'var(--success-600)';
      } else {
        descCounter.style.color = 'var(--text-muted)';
      }
    });
  }

  if (catSelect && previewBox) {
    catSelect.addEventListener('change', () => {
      const selectedId = Number(catSelect.value);
      const cat = State.categories.find(c => c.id === selectedId);
      if (cat) {
        previewBox.style.display = 'flex';
        document.getElementById('preview-dept-name').textContent = cat.departmentName || 'Standard Department';
        document.getElementById('preview-sla-days').textContent = `${cat.slaDays} Days (${cat.slaDays * 24} Hours)`;
      } else {
        previewBox.style.display = 'none';
      }
    });
  }

  if (form) {
    form.onsubmit = handleComplaintSubmit;
  }
}

async function handleComplaintSubmit(e) {
  e.preventDefault();

  // Reset errors
  document.querySelectorAll('.field-error').forEach(el => el.classList.remove('visible'));

  const name = document.getElementById('submit-name').value.trim();
  const email = document.getElementById('submit-email').value.trim();
  const phone = document.getElementById('submit-phone').value.trim();
  const categoryId = document.getElementById('submit-category').value;
  const description = document.getElementById('submit-desc').value.trim();
  const location = document.getElementById('submit-location').value.trim();

  let hasError = false;

  if (!name) {
    showFieldError('error-name', 'Citizen name is required');
    hasError = true;
  }
  if (!email || !email.includes('@')) {
    showFieldError('error-email', 'Valid email address is required');
    hasError = true;
  }
  if (!phone) {
    showFieldError('error-phone', 'Phone number is required');
    hasError = true;
  }
  if (!categoryId) {
    showFieldError('error-category', 'Please select a grievance category');
    hasError = true;
  }
  if (!description || description.length < 10) {
    showFieldError('error-desc', 'Please enter at least 10 characters describing the issue');
    hasError = true;
  }
  if (!location) {
    showFieldError('error-location', 'Location is required');
    hasError = true;
  }

  if (hasError) return;

  const btn = document.getElementById('submit-btn');
  btn.disabled = true;
  btn.textContent = 'Submitting grievance...';

  try {
    const payload = {
      citizenName: name,
      email: email,
      phone: phone,
      categoryId: Number(categoryId),
      description: description,
      location: location
    };

    const res = await API.post(`${API_BASE}/grievances`, payload);

    // Show success dialog
    document.getElementById('success-complaint-code').textContent = res.complaintCode;
    document.getElementById('success-dept-name').textContent = res.department ? res.department.name : 'N/A';
    document.getElementById('success-sla-days').textContent = `${res.slaDays} Days`;
    document.getElementById('success-deadline').textContent = formatDate(res.slaDeadline);

    openModal('modal-submit-success');
    e.target.reset();
    document.getElementById('dept-preview-box').style.display = 'none';
    showToast(`Grievance ${res.complaintCode} logged successfully!`, 'success');
    updateNotificationCount();
  } catch (err) {
    console.error('Submission error:', err);
    showToast(err.message || 'Failed to submit complaint', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Submit Grievance';
  }
}

function showFieldError(id, msg) {
  const el = document.getElementById(id);
  if (el) {
    el.textContent = msg;
    el.classList.add('visible');
  }
}

// ================= TRACK COMPLAINT & TIMELINE =================
async function trackComplaintByCode(code = null) {
  const input = document.getElementById('track-search-input');
  const searchCode = code || (input ? input.value.trim() : '');

  if (!searchCode) {
    showToast('Please enter a Complaint ID (e.g. GRV-000001)', 'warning');
    return;
  }

  const container = document.getElementById('track-results-container');
  container.innerHTML = '<div style="text-align:center; padding:3rem;"><p>Searching grievance records...</p></div>';

  try {
    const grievance = await API.get(`${API_BASE}/grievances/code/${encodeURIComponent(searchCode)}`);
    renderTrackingDetails(grievance);
  } catch (err) {
    container.innerHTML = `
      <div class="stat-card" style="text-align:center; padding:3rem; margin:1rem auto; max-width:600px;">
        <div style="font-size:3rem; margin-bottom:1rem;">🔍</div>
        <h3 style="color:var(--slate-800); margin-bottom:0.5rem;">No Complaint Found</h3>
        <p style="color:var(--text-muted);">Could not locate complaint ID <strong>${escapeHtml(searchCode)}</strong>. Please verify the code and try again.</p>
      </div>
    `;
  }
}

function renderTrackingDetails(g) {
  const container = document.getElementById('track-results-container');
  if (!container) return;

  const isEscalated = g.status === 'ESCALATED' || g.escalation != null;
  const isResolved = g.status === 'RESOLVED';

  // Build Visual Timeline Steps
  let timelineHtml = '';
  if (isEscalated) {
    timelineHtml = `
      <div class="timeline-steps">
        <div class="timeline-step completed">
          <div class="step-node">✓</div>
          <div class="step-label">Complaint Submitted</div>
          <div class="step-time">${formatDate(g.createdAt)}</div>
        </div>
        <div class="timeline-step completed">
          <div class="step-node">✓</div>
          <div class="step-label">Assigned to ${escapeHtml(g.department.name)}</div>
        </div>
        <div class="timeline-step completed">
          <div class="step-node">✓</div>
          <div class="step-label">In Progress</div>
        </div>
        <div class="timeline-step breached">
          <div class="step-node">⚠</div>
          <div class="step-label" style="color:var(--danger-600);">SLA Breached</div>
          <div class="step-time">Deadline: ${formatDate(g.slaDeadline)}</div>
        </div>
        <div class="timeline-step active">
          <div class="step-node">⚡</div>
          <div class="step-label" style="color:var(--danger-600);">Escalated to Senior Officer</div>
          <div class="step-time">${g.escalation ? formatDate(g.escalation.escalatedAt) : 'Recent'}</div>
        </div>
        <div class="timeline-step ${isResolved ? 'completed' : ''}">
          <div class="step-node">${isResolved ? '✓' : '○'}</div>
          <div class="step-label">Resolved</div>
          <div class="step-time">${isResolved ? formatDate(g.resolvedAt) : 'Pending'}</div>
        </div>
      </div>
    `;
  } else {
    const isAssigned = ['ASSIGNED', 'IN_PROGRESS', 'RESOLVED'].includes(g.status);
    const isInProgress = ['IN_PROGRESS', 'RESOLVED'].includes(g.status);

    timelineHtml = `
      <div class="timeline-steps">
        <div class="timeline-step completed">
          <div class="step-node">✓</div>
          <div class="step-label">Complaint Submitted</div>
          <div class="step-time">${formatDate(g.createdAt)}</div>
        </div>
        <div class="timeline-step ${isAssigned ? 'completed' : (g.status === 'PENDING' ? 'active' : '')}">
          <div class="step-node">${isAssigned ? '✓' : (g.status === 'PENDING' ? '●' : '○')}</div>
          <div class="step-label">Assigned to Department</div>
          <div class="step-time">${escapeHtml(g.department.name)}</div>
        </div>
        <div class="timeline-step ${isInProgress ? 'completed' : (g.status === 'ASSIGNED' ? 'active' : '')}">
          <div class="step-node">${isInProgress ? '✓' : (g.status === 'ASSIGNED' ? '●' : '○')}</div>
          <div class="step-label">Under Review & Progress</div>
        </div>
        <div class="timeline-step ${isResolved ? 'completed' : (g.status === 'IN_PROGRESS' ? 'active' : '')}">
          <div class="step-node">${isResolved ? '✓' : (g.status === 'IN_PROGRESS' ? '●' : '○')}</div>
          <div class="step-label">Resolved</div>
          <div class="step-time">${isResolved ? formatDate(g.resolvedAt) : 'Expected by ' + formatDate(g.slaDeadline)}</div>
        </div>
      </div>
    `;
  }

  // Escalation Audit Banner
  let escalationBanner = '';
  if (g.escalation) {
    escalationBanner = `
      <div class="escalation-alert-box">
        <h4>⚡ AUTOMATIC SLA ESCALATION RECORD</h4>
        <p><strong>Senior Officer:</strong> ${escapeHtml(g.escalation.seniorOfficerName)}</p>
        <p><strong>Escalated At:</strong> ${formatDate(g.escalation.escalatedAt)}</p>
        <p><strong>Reason:</strong> ${escapeHtml(g.escalation.reason)}</p>
      </div>
    `;
  }

  // Citizen Rating Card
  let ratingHtml = '';
  if (isResolved) {
    if (g.rating) {
      ratingHtml = `
        <div class="detail-card">
          <h3>Citizen Rating & Feedback</h3>
          <div style="color:#f59e0b; font-size:1.5rem; margin-bottom:0.5rem;">
            ${'★'.repeat(g.rating.rating)}${'☆'.repeat(5 - g.rating.rating)} (${g.rating.rating} / 5)
          </div>
          <p style="font-style:italic; color:var(--slate-700);">"${escapeHtml(g.rating.feedback || 'No comments provided')}"</p>
          <div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.5rem;">Rated on ${formatDate(g.rating.ratedAt)}</div>
        </div>
      `;
    } else {
      ratingHtml = `
        <div class="detail-card">
          <h3>Rate Resolution Experience</h3>
          <p style="font-size:0.875rem; color:var(--text-muted); margin-bottom:1rem;">
            This complaint is resolved. Please share your rating from 1 to 5 stars.
          </p>
          <div class="rating-stars" id="track-rating-stars">
            <span class="star" data-val="1">★</span>
            <span class="star" data-val="2">★</span>
            <span class="star" data-val="3">★</span>
            <span class="star" data-val="4">★</span>
            <span class="star" data-val="5">★</span>
          </div>
          <div class="form-group" style="margin-top:1rem;">
            <textarea id="track-rating-feedback" rows="2" placeholder="Optional comments on how the issue was resolved..."></textarea>
          </div>
          <button class="btn btn-primary btn-sm" onclick="submitRatingForGrievance(${g.id})">Submit Rating</button>
        </div>
      `;
    }
  }

  container.innerHTML = `
    <div class="timeline-container">
      <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem; border-bottom:1px solid var(--border-color); padding-bottom:1rem;">
        <div>
          <span style="font-size:0.8125rem; font-weight:700; color:var(--text-muted); text-transform:uppercase;">Complaint ID</span>
          <div style="font-size:1.75rem; font-weight:800; color:var(--primary-600);">${g.complaintCode}</div>
        </div>
        <div style="display:flex; gap:0.5rem; align-items:center;">
          ${getStatusBadge(g.status)}
          ${getSlaBadge(g.slaStatus)}
        </div>
      </div>
      ${timelineHtml}
    </div>

    ${escalationBanner}

    <div class="detail-grid">
      <div>
        <div class="detail-card">
          <h3>Complaint Information</h3>
          <div class="info-list" style="margin-bottom:1.25rem;">
            <div class="info-item">
              <div class="info-label">Citizen Name</div>
              <div class="info-val">${escapeHtml(g.citizenName)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Category</div>
              <div class="info-val">${escapeHtml(g.category.name)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Created Date</div>
              <div class="info-val">${formatDate(g.createdAt)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Location</div>
              <div class="info-val">${escapeHtml(g.location)}</div>
            </div>
          </div>
          <div class="info-item">
            <div class="info-label">Description</div>
            <div class="info-val" style="font-weight:400; background:var(--slate-50); padding:1rem; border-radius:var(--radius-sm); border:1px solid var(--border-color); margin-top:0.35rem;">
              ${escapeHtml(g.description)}
            </div>
          </div>
        </div>

        <div class="detail-card">
          <h3>Department Information</h3>
          <div class="info-list">
            <div class="info-item">
              <div class="info-label">Department</div>
              <div class="info-val">${escapeHtml(g.department.name)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Officer In Charge</div>
              <div class="info-val">${escapeHtml(g.department.officerName)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Contact / Helpline</div>
              <div class="info-val">${escapeHtml(g.department.contact)}</div>
            </div>
          </div>
        </div>
      </div>

      <div>
        <div class="detail-card">
          <h3>SLA Information</h3>
          <div class="info-list" style="grid-template-columns:1fr;">
            <div class="info-item">
              <div class="info-label">Category SLA</div>
              <div class="info-val">${g.slaDays} Days</div>
            </div>
            <div class="info-item">
              <div class="info-label">Resolution Deadline</div>
              <div class="info-val">${formatDate(g.slaDeadline)}</div>
            </div>
            <div class="info-item">
              <div class="info-label">Current SLA State</div>
              <div class="info-val">${getSlaBadge(g.slaStatus)}</div>
            </div>
            ${g.resolvedAt ? `
              <div class="info-item">
                <div class="info-label">Resolved On</div>
                <div class="info-val" style="color:var(--success-700);">${formatDate(g.resolvedAt)}</div>
              </div>
            ` : ''}
          </div>
        </div>

        ${ratingHtml}
      </div>
    </div>
  `;

  // Attach star rating listener if present
  initStarRating();
}

function initStarRating() {
  const starContainer = document.getElementById('track-rating-stars');
  if (!starContainer) return;

  State.selectedRating = 0;
  const stars = starContainer.querySelectorAll('.star');

  stars.forEach(star => {
    star.addEventListener('click', () => {
      State.selectedRating = Number(star.getAttribute('data-val'));
      stars.forEach(s => {
        const val = Number(s.getAttribute('data-val'));
        s.classList.toggle('selected', val <= State.selectedRating);
      });
    });
  });
}

async function submitRatingForGrievance(grievanceId) {
  if (State.selectedRating < 1 || State.selectedRating > 5) {
    showToast('Please select between 1 and 5 stars for rating', 'warning');
    return;
  }

  const feedbackInput = document.getElementById('track-rating-feedback');
  const feedback = feedbackInput ? feedbackInput.value.trim() : '';

  try {
    await API.post(`${API_BASE}/grievances/${grievanceId}/rating`, {
      rating: State.selectedRating,
      feedback: feedback
    });

    showToast('Thank you! Your feedback has been recorded.', 'success');
    // Refresh track view
    const code = document.getElementById('track-search-input').value.trim();
    trackComplaintByCode(code);
    updateNotificationCount();
  } catch (err) {
    showToast(err.message || 'Failed to submit rating', 'error');
  }
}

// ================= CITIZEN DASHBOARD =================
async function loadCitizenDashboard() {
  try {
    const stats = await API.get(`${API_BASE}/dashboard/stats`);

    document.getElementById('dash-total').textContent = stats.totalComplaints;
    document.getElementById('dash-pending').textContent = stats.pending;
    document.getElementById('dash-inprogress').textContent = stats.inProgress;
    document.getElementById('dash-resolved').textContent = stats.resolved;
    document.getElementById('dash-escalated').textContent = stats.escalated;
    document.getElementById('dash-breached').textContent = stats.slaBreached;

    renderRecentComplaintsTable(stats.recentComplaints || []);
  } catch (err) {
    console.error('Error loading dashboard stats:', err);
  }
}

function renderRecentComplaintsTable(list) {
  const tbody = document.getElementById('citizen-recent-tbody');
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;">No recent complaints logged yet.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(g => `
    <tr>
      <td><span class="table-code" onclick="showView('track', '${g.complaintCode}')">${g.complaintCode}</span></td>
      <td><strong>${escapeHtml(g.category ? g.category.name : 'N/A')}</strong></td>
      <td style="max-width:240px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(g.description)}</td>
      <td>${escapeHtml(g.department ? g.department.name : 'N/A')}</td>
      <td>${getStatusBadge(g.status)}</td>
      <td>${getSlaBadge(g.slaStatus)}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="showView('track', '${g.complaintCode}')">Track</button>
      </td>
    </tr>
  `).join('');
}

// ================= MY COMPLAINTS LIST =================
async function loadMyComplaints() {
  const tbody = document.getElementById('my-complaints-tbody');
  if (!tbody) return;

  tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem;">Loading complaints history...</td></tr>';

  try {
    const params = {
      page: State.myComplaints.page,
      size: State.myComplaints.size,
      sort: State.myComplaints.sort,
      search: State.myComplaints.search,
      status: State.myComplaints.status,
      categoryId: State.myComplaints.categoryId
    };

    const res = await API.get(`${API_BASE}/grievances`, params);

    if (res.content.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem;">No grievances match the search/filter criteria.</td></tr>';
      document.getElementById('my-complaints-pagination-info').textContent = 'Showing 0 of 0';
      return;
    }

    tbody.innerHTML = res.content.map(g => `
      <tr>
        <td><span class="table-code" onclick="showView('track', '${g.complaintCode}')">${g.complaintCode}</span></td>
        <td>${escapeHtml(g.citizenName)}</td>
        <td><strong>${escapeHtml(g.category ? g.category.name : 'N/A')}</strong></td>
        <td>${escapeHtml(g.location)}</td>
        <td>${escapeHtml(g.department ? g.department.name : 'N/A')}</td>
        <td>${getStatusBadge(g.status)}</td>
        <td>${getSlaBadge(g.slaStatus)}</td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="showView('track', '${g.complaintCode}')">View Details</button>
        </td>
      </tr>
    `).join('');

    // Pagination Controls
    const start = res.page * res.size + 1;
    const end = Math.min((res.page + 1) * res.size, res.totalElements);
    document.getElementById('my-complaints-pagination-info').textContent =
      `Showing ${start}-${end} of ${res.totalElements} complaints (Page ${res.page + 1} of ${res.totalPages})`;

    document.getElementById('my-prev-btn').disabled = res.first;
    document.getElementById('my-next-btn').disabled = res.last;
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger-600); padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function handleMyComplaintsSearch(term) {
  State.myComplaints.search = term;
  State.myComplaints.page = 0;
  loadMyComplaints();
}

function handleMyComplaintsFilter(status) {
  State.myComplaints.status = status;
  State.myComplaints.page = 0;
  loadMyComplaints();
}

function handleMyComplaintsCatFilter(catId) {
  State.myComplaints.categoryId = catId;
  State.myComplaints.page = 0;
  loadMyComplaints();
}

function handleMyComplaintsSort(sortVal) {
  State.myComplaints.sort = sortVal;
  State.myComplaints.page = 0;
  loadMyComplaints();
}

function prevMyPage() {
  if (State.myComplaints.page > 0) {
    State.myComplaints.page--;
    loadMyComplaints();
  }
}

function nextMyPage() {
  State.myComplaints.page++;
  loadMyComplaints();
}

// ================= OFFICER DASHBOARD =================
async function loadOfficerDashboard() {
  try {
    const stats = await API.get(`${API_BASE}/dashboard/stats`);

    document.getElementById('off-total').textContent = stats.totalComplaints;
    document.getElementById('off-pending').textContent = stats.pending;
    document.getElementById('off-assigned').textContent = stats.assigned;
    document.getElementById('off-inprogress').textContent = stats.inProgress;
    document.getElementById('off-resolved').textContent = stats.resolved;
    document.getElementById('off-escalated').textContent = stats.escalated;

    loadOfficerComplaints();
  } catch (err) {
    console.error('Failed to load officer dashboard:', err);
  }
}

async function loadOfficerComplaints() {
  const tbody = document.getElementById('officer-complaints-tbody');
  if (!tbody) return;

  try {
    const res = await API.get(`${API_BASE}/grievances`, {
      page: State.officerComplaints.page,
      size: State.officerComplaints.size,
      sort: State.officerComplaints.sort,
      status: State.officerComplaints.status
    });

    if (res.content.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem;">No grievances under this queue.</td></tr>';
      return;
    }

    tbody.innerHTML = res.content.map(g => `
      <tr>
        <td><span class="table-code" onclick="showView('track', '${g.complaintCode}')">${g.complaintCode}</span></td>
        <td><strong>${escapeHtml(g.category.name)}</strong></td>
        <td>${escapeHtml(g.location)}</td>
        <td>${getStatusBadge(g.status)}</td>
        <td>${formatDate(g.slaDeadline)}</td>
        <td>${getSlaBadge(g.slaStatus)}</td>
        <td style="white-space:nowrap;">
          <button class="btn btn-primary btn-sm" onclick="openStatusUpdateModal(${g.id})">Update Status</button>
          <button class="btn btn-outline btn-sm" onclick="showView('track', '${g.complaintCode}')">View</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger-600); padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

async function openStatusUpdateModal(grievanceId) {
  try {
    const g = await API.get(`${API_BASE}/grievances/${grievanceId}`);
    State.currentEditingComplaint = g;

    document.getElementById('modal-update-code').textContent = g.complaintCode;
    document.getElementById('modal-current-status').innerHTML = getStatusBadge(g.status);

    const select = document.getElementById('modal-target-status');
    select.innerHTML = '<option value="">-- Select New Status --</option>';

    if (g.allowedNextStatuses && g.allowedNextStatuses.length > 0) {
      g.allowedNextStatuses.forEach(s => {
        select.innerHTML += `<option value="${s}">${s}</option>`;
      });
      document.getElementById('modal-update-submit-btn').disabled = false;
    } else {
      select.innerHTML = '<option value="">No transitions allowed (Terminal State)</option>';
      document.getElementById('modal-update-submit-btn').disabled = true;
    }

    document.getElementById('modal-update-remarks').value = '';
    document.getElementById('modal-update-officer').value = g.department ? g.department.officerName : '';

    openModal('modal-update-status');
  } catch (err) {
    showToast(err.message || 'Failed to open status updater', 'error');
  }
}

async function handleStatusUpdateSubmit() {
  const g = State.currentEditingComplaint;
  if (!g) return;

  const targetStatus = document.getElementById('modal-target-status').value;
  const remarks = document.getElementById('modal-update-remarks').value.trim();
  const officerName = document.getElementById('modal-update-officer').value.trim();

  if (!targetStatus) {
    showToast('Please select a valid target status', 'warning');
    return;
  }

  try {
    await API.put(`${API_BASE}/grievances/${g.id}/status`, {
      status: targetStatus,
      remarks: remarks,
      officerName: officerName
    });

    closeModal('modal-update-status');
    showToast(`Status updated to ${targetStatus} successfully!`, 'success');
    loadOfficerDashboard();
    updateNotificationCount();
  } catch (err) {
    showToast(err.message || 'Status transition rejected by server', 'error');
  }
}

// ================= ADMIN DASHBOARD =================
async function loadAdminDashboard() {
  try {
    const stats = await API.get(`${API_BASE}/dashboard/stats`);

    document.getElementById('adm-total').textContent = stats.totalComplaints;
    document.getElementById('adm-pending').textContent = stats.pending;
    document.getElementById('adm-inprogress').textContent = stats.inProgress;
    document.getElementById('adm-resolved').textContent = stats.resolved;
    document.getElementById('adm-escalated').textContent = stats.escalated;
    document.getElementById('adm-breached').textContent = stats.slaBreached;

    renderChart('chart-categories', stats.complaintsByCategory);
    renderChart('chart-statuses', stats.complaintsByStatus);
    renderChart('chart-escalations', stats.escalationsByDepartment);

    loadCategoriesTable();
    loadDepartmentsTable();
    loadEscalationsTable();
  } catch (err) {
    console.error('Failed to load admin dashboard:', err);
  }
}

function renderChart(elementId, dataMap) {
  const container = document.getElementById(elementId);
  if (!container || !dataMap) return;

  const entries = Object.entries(dataMap);
  if (entries.length === 0) {
    container.innerHTML = '<p style="color:var(--text-muted); font-size:0.875rem;">No data available</p>';
    return;
  }

  const maxVal = Math.max(...entries.map(e => e[1]), 1);

  container.innerHTML = entries.map(([key, count]) => {
    const pct = Math.round((count / maxVal) * 100);
    return `
      <div class="bar-row">
        <div class="bar-meta">
          <span>${escapeHtml(key)}</span>
          <span>${count}</span>
        </div>
        <div class="bar-track">
          <div class="bar-fill" style="width: ${pct}%;"></div>
        </div>
      </div>
    `;
  }).join('');
}

// Category Management
async function loadCategoriesTable() {
  const tbody = document.getElementById('admin-categories-tbody');
  if (!tbody) return;

  try {
    const cats = await API.get(`${API_BASE}/categories`);
    tbody.innerHTML = cats.map(c => `
      <tr>
        <td><strong>${escapeHtml(c.name)}</strong></td>
        <td><span class="sla-chip">${c.slaDays} Days</span></td>
        <td>${escapeHtml(c.departmentName || 'N/A')}</td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="openEditCategoryModal(${c.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="deleteCategory(${c.id})">Delete</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="4" style="color:var(--danger-600); padding:1rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function openAddCategoryModal() {
  document.getElementById('modal-cat-title').textContent = 'Add New Category';
  document.getElementById('modal-cat-id').value = '';
  document.getElementById('modal-cat-name').value = '';
  document.getElementById('modal-cat-sla').value = '3';

  const deptSelect = document.getElementById('modal-cat-dept');
  deptSelect.innerHTML = State.departments.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join('');

  openModal('modal-category');
}

async function openEditCategoryModal(id) {
  try {
    const cat = await API.get(`${API_BASE}/categories/${id}`);
    document.getElementById('modal-cat-title').textContent = 'Edit Category';
    document.getElementById('modal-cat-id').value = cat.id;
    document.getElementById('modal-cat-name').value = cat.name;
    document.getElementById('modal-cat-sla').value = cat.slaDays;

    const deptSelect = document.getElementById('modal-cat-dept');
    deptSelect.innerHTML = State.departments.map(d =>
      `<option value="${d.id}" ${d.id === cat.departmentId ? 'selected' : ''}>${escapeHtml(d.name)}</option>`
    ).join('');

    openModal('modal-category');
  } catch (err) {
    showToast(err.message || 'Error loading category', 'error');
  }
}

async function handleSaveCategory() {
  const id = document.getElementById('modal-cat-id').value;
  const name = document.getElementById('modal-cat-name').value.trim();
  const slaDays = Number(document.getElementById('modal-cat-sla').value);
  const departmentId = Number(document.getElementById('modal-cat-dept').value);

  if (!name || slaDays <= 0 || !departmentId) {
    showToast('Name is required and SLA must be positive', 'warning');
    return;
  }

  try {
    if (id) {
      await API.put(`${API_BASE}/categories/${id}`, { name, slaDays, departmentId });
      showToast('Category updated successfully!', 'success');
    } else {
      await API.post(`${API_BASE}/categories`, { name, slaDays, departmentId });
      showToast('Category created successfully!', 'success');
    }

    closeModal('modal-category');
    loadCategoriesTable();
    loadMetadata();
  } catch (err) {
    showToast(err.message || 'Failed to save category', 'error');
  }
}

async function deleteCategory(id) {
  if (!confirm('Are you sure you want to delete this category?')) return;
  try {
    await API.delete(`${API_BASE}/categories/${id}`);
    showToast('Category deleted successfully', 'success');
    loadCategoriesTable();
    loadMetadata();
  } catch (err) {
    showToast(err.message || 'Cannot delete category (active complaints exist)', 'error');
  }
}

// Department Management
async function loadDepartmentsTable() {
  const tbody = document.getElementById('admin-departments-tbody');
  if (!tbody) return;

  try {
    const depts = await API.get(`${API_BASE}/departments`);
    tbody.innerHTML = depts.map(d => `
      <tr>
        <td><strong>${escapeHtml(d.name)}</strong></td>
        <td>${escapeHtml(d.officerName)}</td>
        <td>${escapeHtml(d.contact)}</td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="openEditDepartmentModal(${d.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="deleteDepartment(${d.id})">Delete</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="4" style="color:var(--danger-600); padding:1rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function openAddDepartmentModal() {
  document.getElementById('modal-dept-title').textContent = 'Add New Department';
  document.getElementById('modal-dept-id').value = '';
  document.getElementById('modal-dept-name').value = '';
  document.getElementById('modal-dept-officer').value = '';
  document.getElementById('modal-dept-contact').value = '';

  openModal('modal-department');
}

async function openEditDepartmentModal(id) {
  try {
    const dept = await API.get(`${API_BASE}/departments/${id}`);
    document.getElementById('modal-dept-title').textContent = 'Edit Department';
    document.getElementById('modal-dept-id').value = dept.id;
    document.getElementById('modal-dept-name').value = dept.name;
    document.getElementById('modal-dept-officer').value = dept.officerName;
    document.getElementById('modal-dept-contact').value = dept.contact;

    openModal('modal-department');
  } catch (err) {
    showToast(err.message || 'Error loading department', 'error');
  }
}

async function handleSaveDepartment() {
  const id = document.getElementById('modal-dept-id').value;
  const name = document.getElementById('modal-dept-name').value.trim();
  const officerName = document.getElementById('modal-dept-officer').value.trim();
  const contact = document.getElementById('modal-dept-contact').value.trim();

  if (!name || !officerName || !contact) {
    showToast('All department fields are required', 'warning');
    return;
  }

  try {
    if (id) {
      await API.put(`${API_BASE}/departments/${id}`, { name, officerName, contact });
      showToast('Department updated successfully!', 'success');
    } else {
      await API.post(`${API_BASE}/departments`, { name, officerName, contact });
      showToast('Department created successfully!', 'success');
    }

    closeModal('modal-department');
    loadDepartmentsTable();
    loadMetadata();
  } catch (err) {
    showToast(err.message || 'Failed to save department', 'error');
  }
}

async function deleteDepartment(id) {
  if (!confirm('Are you sure you want to delete this department?')) return;
  try {
    await API.delete(`${API_BASE}/departments/${id}`);
    showToast('Department deleted successfully', 'success');
    loadDepartmentsTable();
    loadMetadata();
  } catch (err) {
    showToast(err.message || 'Cannot delete department with linked categories or grievances', 'error');
  }
}

// Escalations Audit Trail
async function loadEscalationsTable() {
  const tbody = document.getElementById('admin-escalations-tbody');
  if (!tbody) return;

  try {
    const list = await API.get(`${API_BASE}/escalations`);
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No complaints currently escalated.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(e => `
      <tr>
        <td><strong>${escapeHtml(e.complaintCode)}</strong></td>
        <td><span style="color:var(--danger-700); font-weight:600;">${escapeHtml(e.seniorOfficerName)}</span></td>
        <td>${formatDate(e.escalatedAt)}</td>
        <td><span class="badge badge-escalated">${escapeHtml(e.reason)}</span></td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="showView('track', '${e.complaintCode}')">Inspect</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="color:var(--danger-600); padding:1rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

// ================= NOTIFICATIONS DRAWER =================
async function toggleNotificationDrawer() {
  const drawer = document.getElementById('notification-drawer');
  const backdrop = document.getElementById('drawer-backdrop');

  const isOpen = drawer.classList.contains('open');
  if (isOpen) {
    drawer.classList.remove('open');
    backdrop.classList.remove('active');
  } else {
    drawer.classList.add('open');
    backdrop.classList.add('active');
    loadNotificationsList();
  }
}

async function loadNotificationsList() {
  const body = document.getElementById('drawer-notifications-body');
  if (!body) return;

  body.innerHTML = '<p style="text-align:center; padding:2rem; color:var(--text-muted);">Loading notifications...</p>';

  try {
    const list = await API.get(`${API_BASE}/notifications`);
    if (list.length === 0) {
      body.innerHTML = '<p style="text-align:center; padding:2rem; color:var(--text-muted);">No notifications yet.</p>';
      return;
    }

    body.innerHTML = list.map(n => `
      <div class="notif-item ${n.read ? '' : 'unread'}" onclick="markNotificationRead(${n.id}, '${n.complaintCode || ''}')">
        <div class="notif-msg">${escapeHtml(n.message)}</div>
        <div class="notif-time">${formatDate(n.createdAt)}</div>
      </div>
    `).join('');
  } catch (err) {
    body.innerHTML = `<p style="color:var(--danger-600); padding:1rem;">Error: ${escapeHtml(err.message)}</p>`;
  }
}

async function markNotificationRead(id, complaintCode) {
  try {
    await API.put(`${API_BASE}/notifications/${id}/read`);
    updateNotificationCount();
    loadNotificationsList();
    if (complaintCode) {
      toggleNotificationDrawer();
      showView('track', complaintCode);
    }
  } catch (err) {
    console.error('Error marking notification read:', err);
  }
}

async function markAllNotificationsAsRead() {
  try {
    await API.put(`${API_BASE}/notifications/read-all`);
    updateNotificationCount();
    loadNotificationsList();
    showToast('All notifications marked as read', 'success');
  } catch (err) {
    showToast('Failed to mark notifications read', 'error');
  }
}

async function updateNotificationCount() {
  try {
    const res = await API.get(`${API_BASE}/notifications/unread-count`);
    const badge = document.getElementById('notif-badge');
    if (badge) {
      const count = res.unreadCount || 0;
      badge.textContent = count;
      badge.style.display = count > 0 ? 'flex' : 'none';
    }
  } catch (err) {
    // Silently handle count check
  }
}

// ================= MODAL CONTROLS =================
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('active');
}

// Close modals when clicking backdrop
window.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-backdrop')) {
    e.target.classList.remove('active');
  }
});

// ================= APP INITIALIZATION =================
document.addEventListener('DOMContentLoaded', () => {
  loadMetadata();
  showView('landing');

  // Set up periodic notification refresh
  setInterval(() => {
    updateNotificationCount();
  }, 20000);
});
