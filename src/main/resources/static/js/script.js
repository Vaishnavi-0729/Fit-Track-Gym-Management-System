// ===================== Setup =====================
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

const api = {
    async request(url, options = {}) {
        const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        const res = await fetch(url, { ...options, headers });
        if (res.status === 204) return null;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            const message = data?.message || 'Request failed';
            throw new Error(message);
        }
        return data;
    },
    getMembers: (params = '') => api.request(`/api/members${params}`),
    getMember: (id) => api.request(`/api/members/${id}`),
    createMember: (body) => api.request('/api/members', { method: 'POST', body: JSON.stringify(body) }),
    updateMember: (id, body) => api.request(`/api/members/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    deleteMember: (id) => api.request(`/api/members/${id}`, { method: 'DELETE' }),
    renewMember: (id) => api.request(`/api/members/${id}/renew`, { method: 'POST' }),
    cancelMember: (id) => api.request(`/api/members/${id}/cancel`, { method: 'POST' }),
    getStats: () => api.request('/api/members/stats'),

    getPlans: () => api.request('/api/plans'),
    createPlan: (body) => api.request('/api/plans', { method: 'POST', body: JSON.stringify(body) }),
    updatePlan: (id, body) => api.request(`/api/plans/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    deletePlan: (id) => api.request(`/api/plans/${id}`, { method: 'DELETE' }),
};

let allPlans = [];

// ===================== Navigation =====================
document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => switchView(btn.dataset.view));
});

function switchView(view) {
    document.querySelectorAll('.nav-item').forEach(b => b.classList.toggle('active', b.dataset.view === view));
    document.querySelectorAll('.view').forEach(v => v.classList.toggle('active', v.id === `view-${view}`));
    if (view === 'dashboard') loadDashboard();
    if (view === 'members') loadMembers();
    if (view === 'plans') loadPlans();
}

// ===================== Toast =====================
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    setTimeout(() => toast.classList.remove('show'), 3000);
}

// ===================== Modal helpers =====================
document.querySelectorAll('[data-close]').forEach(el => {
    el.addEventListener('click', () => closeModal(el.dataset.close));
});
function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

// ===================== Dashboard =====================
async function loadDashboard() {
    try {
        const [stats, members, plans] = await Promise.all([api.getStats(), api.getMembers(), api.getPlans()]);
        document.getElementById('statTotal').textContent = stats.total;
        document.getElementById('statActive').textContent = stats.active;
        document.getElementById('statExpired').textContent = stats.expired;
        document.getElementById('statPlans').textContent = plans.length;

        const recent = [...members]
            .sort((a, b) => new Date(b.joiningDate) - new Date(a.joiningDate))
            .slice(0, 5);

        const tbody = document.querySelector('#recentMembersTable tbody');
        tbody.innerHTML = recent.length ? recent.map(m => `
            <tr>
                <td>${escapeHtml(m.fullName)}</td>
                <td>${escapeHtml(m.email)}</td>
                <td>${escapeHtml(m.membershipPlan?.name || '-')}</td>
                <td>${m.joiningDate}</td>
                <td><span class="badge badge-${m.status}">${m.status}</span></td>
            </tr>
        `).join('') : `<tr><td colspan="5" class="empty-state">No members yet</td></tr>`;
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ===================== Members =====================
async function loadMembers() {
    try {
        const search = document.getElementById('memberSearch').value;
        const status = document.getElementById('statusFilter').value;
        let query = '';
        if (search) query = `?search=${encodeURIComponent(search)}`;
        else if (status) query = `?status=${status}`;

        const members = await api.getMembers(query);
        renderMembersTable(members);
        if (allPlans.length === 0) allPlans = await api.getPlans();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderMembersTable(members) {
    const tbody = document.querySelector('#membersTable tbody');
    if (!members.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No members found</td></tr>`;
        return;
    }
    tbody.innerHTML = members.map(m => `
        <tr>
            <td>${escapeHtml(m.fullName)}</td>
            <td>${escapeHtml(m.email)}</td>
            <td>${escapeHtml(m.phoneNumber)}</td>
            <td>${escapeHtml(m.membershipPlan?.name || '-')}</td>
            <td>${m.joiningDate}</td>
            <td>${m.membershipEndDate || '-'}</td>
            <td><span class="badge badge-${m.status}">${m.status}</span></td>
            <td class="action-btns">
                <button class="btn-icon" title="Edit" onclick="openEditMember(${m.id})">✏️</button>
                <button class="btn-icon" title="Renew" onclick="handleRenew(${m.id})">🔄</button>
                <button class="btn-icon" title="Cancel" onclick="handleCancel(${m.id})">🚫</button>
                <button class="btn-icon" title="Delete" onclick="handleDeleteMember(${m.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

document.getElementById('memberSearch').addEventListener('input', debounce(loadMembers, 300));
document.getElementById('statusFilter').addEventListener('change', loadMembers);

document.getElementById('btnAddMember').addEventListener('click', async () => {
    await openMemberModal();
});

async function openMemberModal() {
    document.getElementById('memberModalTitle').textContent = 'Register Member';
    document.getElementById('memberForm').reset();
    document.getElementById('memberId').value = '';
    document.getElementById('memberFormError').textContent = '';
    document.getElementById('joiningDate').value = new Date().toISOString().slice(0, 10);
    await populatePlanSelect();
    openModal('memberModalOverlay');
}

async function openEditMember(id) {
    try {
        const member = await api.getMember(id);
        document.getElementById('memberModalTitle').textContent = 'Edit Member';
        document.getElementById('memberFormError').textContent = '';
        document.getElementById('memberId').value = member.id;
        document.getElementById('fullName').value = member.fullName;
        document.getElementById('email').value = member.email;
        document.getElementById('phoneNumber').value = member.phoneNumber;
        document.getElementById('gender').value = member.gender || '';
        document.getElementById('dateOfBirth').value = member.dateOfBirth || '';
        document.getElementById('joiningDate').value = member.joiningDate;
        document.getElementById('address').value = member.address || '';
        await populatePlanSelect();
        document.getElementById('membershipPlanId').value = member.membershipPlan.id;
        openModal('memberModalOverlay');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function populatePlanSelect() {
    if (allPlans.length === 0) allPlans = await api.getPlans();
    const select = document.getElementById('membershipPlanId');
    select.innerHTML = allPlans.map(p => `<option value="${p.id}">${escapeHtml(p.name)} ($${p.price}/${p.durationInMonths}mo)</option>`).join('');
}

document.getElementById('memberForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById('memberFormError');
    errorEl.textContent = '';

    const id = document.getElementById('memberId').value;
    const body = {
        fullName: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        phoneNumber: document.getElementById('phoneNumber').value.trim(),
        gender: document.getElementById('gender').value,
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        joiningDate: document.getElementById('joiningDate').value,
        address: document.getElementById('address').value.trim(),
        membershipPlan: { id: Number(document.getElementById('membershipPlanId').value) }
    };

    try {
        if (id) {
            await api.updateMember(id, body);
            showToast('Member updated successfully');
        } else {
            await api.createMember(body);
            showToast('Member registered successfully');
        }
        closeModal('memberModalOverlay');
        loadMembers();
        loadDashboard();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

async function handleDeleteMember(id) {
    if (!confirm('Delete this member? This cannot be undone.')) return;
    try {
        await api.deleteMember(id);
        showToast('Member deleted');
        loadMembers();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleRenew(id) {
    try {
        await api.renewMember(id);
        showToast('Membership renewed');
        loadMembers();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleCancel(id) {
    if (!confirm('Cancel this membership?')) return;
    try {
        await api.cancelMember(id);
        showToast('Membership cancelled');
        loadMembers();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ===================== Plans =====================
async function loadPlans() {
    try {
        allPlans = await api.getPlans();
        renderPlanCards(allPlans);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderPlanCards(plans) {
    const container = document.getElementById('planCards');
    if (!plans.length) {
        container.innerHTML = `<div class="empty-state">No membership plans yet</div>`;
        return;
    }
    container.innerHTML = plans.map(p => `
        <div class="plan-card">
            <h3>${escapeHtml(p.name)}</h3>
            <div class="plan-price">$${Number(p.price).toFixed(2)}</div>
            <div class="plan-duration">${p.durationInMonths} month(s)</div>
            <div class="plan-desc">${escapeHtml(p.description || '')}</div>
            <div class="plan-actions">
                <button class="btn btn-secondary" onclick="openEditPlan(${p.id})">Edit</button>
                <button class="btn btn-secondary" onclick="handleDeletePlan(${p.id})">Delete</button>
            </div>
        </div>
    `).join('');
}

document.getElementById('btnAddPlan').addEventListener('click', () => {
    document.getElementById('planModalTitle').textContent = 'Add Membership Plan';
    document.getElementById('planForm').reset();
    document.getElementById('planId').value = '';
    document.getElementById('planFormError').textContent = '';
    openModal('planModalOverlay');
});

async function openEditPlan(id) {
    const plan = allPlans.find(p => p.id === id);
    if (!plan) return;
    document.getElementById('planModalTitle').textContent = 'Edit Membership Plan';
    document.getElementById('planFormError').textContent = '';
    document.getElementById('planId').value = plan.id;
    document.getElementById('planName').value = plan.name;
    document.getElementById('planDuration').value = plan.durationInMonths;
    document.getElementById('planPrice').value = plan.price;
    document.getElementById('planDescription').value = plan.description || '';
    openModal('planModalOverlay');
}

document.getElementById('planForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById('planFormError');
    errorEl.textContent = '';

    const id = document.getElementById('planId').value;
    const body = {
        name: document.getElementById('planName').value.trim(),
        durationInMonths: Number(document.getElementById('planDuration').value),
        price: Number(document.getElementById('planPrice').value),
        description: document.getElementById('planDescription').value.trim()
    };

    try {
        if (id) {
            await api.updatePlan(id, body);
            showToast('Plan updated successfully');
        } else {
            await api.createPlan(body);
            showToast('Plan created successfully');
        }
        closeModal('planModalOverlay');
        loadPlans();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

async function handleDeletePlan(id) {
    if (!confirm('Delete this membership plan?')) return;
    try {
        await api.deletePlan(id);
        showToast('Plan deleted');
        loadPlans();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ===================== Utils =====================
function debounce(fn, delay) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ===================== Init =====================
loadDashboard();
