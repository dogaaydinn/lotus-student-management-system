<template>
  <div class="analytics-dashboard">
    <div class="dashboard-header">
      <h1>📊 Analytics & Business Intelligence</h1>
      <p class="subtitle">Real-time insights and predictive analytics</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading analytics data...</p>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-container">
      <p>⚠️ {{ error }}</p>
      <button @click="loadAllData" class="retry-btn">Retry</button>
    </div>

    <!-- Dashboard Content -->
    <div v-if="!loading && !error" class="dashboard-content">

      <!-- Key Metrics Cards -->
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-icon">👥</div>
          <div class="metric-content">
            <h3>Total Students</h3>
            <p class="metric-value">{{ enrollmentStats?.totalStudents || 0 }}</p>
            <span class="metric-change positive">+12% from last month</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">🎯</div>
          <div class="metric-content">
            <h3>Placement Rate</h3>
            <p class="metric-value">{{ (placementStats?.placementRate || 0).toFixed(1) }}%</p>
            <span class="metric-change positive">+5% from last quarter</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">💼</div>
          <div class="metric-content">
            <h3>Placed Students</h3>
            <p class="metric-value">{{ placementStats?.placedStudents || 0 }}</p>
            <span class="metric-change positive">{{ placementStats?.totalStudents || 0 }} total</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon">📈</div>
          <div class="metric-content">
            <h3>Avg Applications</h3>
            <p class="metric-value">4.2</p>
            <span class="metric-change neutral">per student</span>
          </div>
        </div>
      </div>

      <!-- Charts Row 1 -->
      <div class="charts-row">
        <!-- Enrollment by Faculty -->
        <div class="chart-card">
          <h3>👥 Enrollment by Faculty</h3>
          <canvas ref="facultyChart"></canvas>
        </div>

        <!-- Internship Status -->
        <div class="chart-card">
          <h3>💼 Internship Status Distribution</h3>
          <canvas ref="internshipChart"></canvas>
        </div>
      </div>

      <!-- Charts Row 2 -->
      <div class="charts-row">
        <!-- Enrollment Trend -->
        <div class="chart-card full-width">
          <h3>📈 Enrollment Trend (Last 12 Months)</h3>
          <canvas ref="trendChart"></canvas>
        </div>
      </div>

      <!-- Charts Row 3 -->
      <div class="charts-row">
        <!-- Top Recruiting Companies -->
        <div class="chart-card">
          <h3>🏢 Top Recruiting Companies</h3>
          <div class="companies-list">
            <div
              v-for="(company, index) in placementStats?.topCompanies || []"
              :key="index"
              class="company-item"
            >
              <span class="company-rank">#{{ index + 1 }}</span>
              <span class="company-name">{{ company.companyName || 'Unknown' }}</span>
              <span class="company-count">{{ company.placements }} hires</span>
            </div>
            <div v-if="!placementStats?.topCompanies?.length" class="no-data">
              No placement data available yet
            </div>
          </div>
        </div>

        <!-- Enrollment by Department -->
        <div class="chart-card">
          <h3>🎓 Top 10 Departments</h3>
          <canvas ref="departmentChart"></canvas>
        </div>
      </div>

      <!-- Custom Report Section -->
      <div class="custom-report-section">
        <h2>📋 Custom Report Generator</h2>

        <div class="report-filters">
          <div class="filter-group">
            <label>Faculty:</label>
            <select v-model="reportFilters.faculty">
              <option value="">All Faculties</option>
              <option v-for="(count, faculty) in enrollmentStats?.byFaculty" :key="faculty" :value="faculty">
                {{ faculty }} ({{ count }})
              </option>
            </select>
          </div>

          <div class="filter-group">
            <label>Department:</label>
            <select v-model="reportFilters.department">
              <option value="">All Departments</option>
              <option v-for="(count, dept) in enrollmentStats?.byDepartment" :key="dept" :value="dept">
                {{ dept }} ({{ count }})
              </option>
            </select>
          </div>

          <div class="filter-group">
            <label>From Date:</label>
            <input type="date" v-model="reportFilters.dateFrom" />
          </div>

          <div class="filter-group">
            <label>To Date:</label>
            <input type="date" v-model="reportFilters.dateTo" />
          </div>

          <button @click="generateCustomReport" class="generate-btn" :disabled="generatingReport">
            <span v-if="!generatingReport">📊 Generate Report</span>
            <span v-else>⏳ Generating...</span>
          </button>
        </div>

        <!-- Custom Report Results -->
        <div v-if="customReport" class="report-results">
          <h3>Report Results ({{ customReport.data?.length || 0 }} records)</h3>
          <div class="report-summary">
            <p><strong>Generated at:</strong> {{ new Date(customReport.generatedAt).toLocaleString() }}</p>
            <p><strong>Total Records:</strong> {{ customReport.summary?.totalRecords || 0 }}</p>
          </div>
          <button @click="exportReport('csv')" class="export-btn">💾 Export as CSV</button>
          <button @click="exportReport('pdf')" class="export-btn">📄 Export as PDF</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import axios from 'axios'

// Register Chart.js components
Chart.register(...registerables)

// Reactive state
const loading = ref(false)
const error = ref(null)
const enrollmentStats = ref(null)
const placementStats = ref(null)
const trendData = ref(null)
const customReport = ref(null)
const generatingReport = ref(false)

// Chart references
const facultyChart = ref(null)
const internshipChart = ref(null)
const trendChart = ref(null)
const departmentChart = ref(null)

// Chart instances
let facultyChartInstance = null
let internshipChartInstance = null
let trendChartInstance = null
let departmentChartInstance = null

// Report filters
const reportFilters = ref({
  faculty: '',
  department: '',
  dateFrom: '',
  dateTo: ''
})

// Load all analytics data
const loadAllData = async () => {
  loading.value = true
  error.value = null

  try {
    // Load enrollment statistics
    const enrollmentRes = await axios.get('/api/analytics/enrollment')
    enrollmentStats.value = enrollmentRes.data

    // Load placement statistics
    const placementRes = await axios.get('/api/analytics/placement')
    placementStats.value = placementRes.data

    // Load enrollment trend (last 12 months)
    const trendRes = await axios.get('/api/analytics/trend?months=12')
    trendData.value = trendRes.data

    // Wait for next tick to ensure DOM is updated
    await nextTick()

    // Create charts
    createCharts()
  } catch (err) {
    console.error('Error loading analytics data:', err)
    error.value = 'Failed to load analytics data. Please try again.'
  } finally {
    loading.value = false
  }
}

// Create all charts
const createCharts = () => {
  createFacultyChart()
  createInternshipChart()
  createTrendChart()
  createDepartmentChart()
}

// Faculty Chart (Pie)
const createFacultyChart = () => {
  if (!facultyChart.value || !enrollmentStats.value?.byFaculty) return

  const data = enrollmentStats.value.byFaculty
  const labels = Object.keys(data)
  const values = Object.values(data)

  const ctx = facultyChart.value.getContext('2d')

  if (facultyChartInstance) {
    facultyChartInstance.destroy()
  }

  facultyChartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: [
          '#FF6384',
          '#36A2EB',
          '#FFCE56',
          '#4BC0C0',
          '#9966FF',
          '#FF9F40'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom'
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const label = context.label || ''
              const value = context.parsed || 0
              const total = context.dataset.data.reduce((a, b) => a + b, 0)
              const percentage = ((value / total) * 100).toFixed(1)
              return `${label}: ${value} (${percentage}%)`
            }
          }
        }
      }
    }
  })
}

// Internship Status Chart (Doughnut)
const createInternshipChart = () => {
  if (!internshipChart.value || !enrollmentStats.value?.byInternshipStatus) return

  const data = enrollmentStats.value.byInternshipStatus
  const labels = Object.keys(data)
  const values = Object.values(data)

  const ctx = internshipChart.value.getContext('2d')

  if (internshipChartInstance) {
    internshipChartInstance.destroy()
  }

  internshipChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: [
          '#4CAF50',
          '#FFC107',
          '#F44336',
          '#2196F3'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    }
  })
}

// Enrollment Trend Chart (Line)
const createTrendChart = () => {
  if (!trendChart.value || !trendData.value) return

  const labels = trendData.value.map(item => item.month)
  const values = trendData.value.map(item => item.count)

  const ctx = trendChart.value.getContext('2d')

  if (trendChartInstance) {
    trendChartInstance.destroy()
  }

  trendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'New Enrollments',
        data: values,
        borderColor: '#2196F3',
        backgroundColor: 'rgba(33, 150, 243, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  })
}

// Department Chart (Horizontal Bar)
const createDepartmentChart = () => {
  if (!departmentChart.value || !enrollmentStats.value?.byDepartment) return

  const data = enrollmentStats.value.byDepartment
  const labels = Object.keys(data).slice(0, 10) // Top 10
  const values = Object.values(data).slice(0, 10)

  const ctx = departmentChart.value.getContext('2d')

  if (departmentChartInstance) {
    departmentChartInstance.destroy()
  }

  departmentChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: 'Students',
        data: values,
        backgroundColor: '#4CAF50'
      }]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          beginAtZero: true
        }
      }
    }
  })
}

// Generate custom report
const generateCustomReport = async () => {
  generatingReport.value = true

  try {
    const response = await axios.post('/api/analytics/report/custom', reportFilters.value)
    customReport.value = response.data
  } catch (err) {
    console.error('Error generating report:', err)
    alert('Failed to generate report. Please try again.')
  } finally {
    generatingReport.value = false
  }
}

// Export report
const exportReport = (format) => {
  if (!customReport.value) return

  if (format === 'csv') {
    // Export as CSV
    const data = customReport.value.data
    if (!data || data.length === 0) {
      alert('No data to export')
      return
    }

    const headers = Object.keys(data[0])
    const csvContent = [
      headers.join(','),
      ...data.map(row => headers.map(header => row[header]).join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `lotus-report-${Date.now()}.csv`
    a.click()
  } else if (format === 'pdf') {
    alert('PDF export coming soon!')
  }
}

// Lifecycle
onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.analytics-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 80px 20px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #2196F3;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container p {
  color: #f44336;
  font-size: 18px;
  margin-bottom: 16px;
}

.retry-btn {
  background: #2196F3;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.retry-btn:hover {
  background: #1976D2;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric-icon {
  font-size: 40px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 12px;
}

.metric-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.metric-value {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
}

.metric-change {
  font-size: 12px;
  font-weight: 500;
}

.metric-change.positive {
  color: #4CAF50;
}

.metric-change.neutral {
  color: #666;
}

/* Charts */
.charts-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.chart-card canvas {
  max-height: 400px;
}

/* Companies List */
.companies-list {
  margin-top: 16px;
}

.company-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 8px;
}

.company-rank {
  font-weight: 700;
  color: #2196F3;
  min-width: 30px;
}

.company-name {
  flex: 1;
  font-weight: 500;
}

.company-count {
  color: #666;
  font-size: 14px;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px;
}

/* Custom Report */
.custom-report-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.custom-report-section h2 {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.report-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.filter-group select,
.filter-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.generate-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
}

.generate-btn:hover:not(:disabled) {
  background: #45a049;
}

.generate-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.report-results {
  margin-top: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.report-results h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}

.report-summary {
  margin-bottom: 16px;
}

.report-summary p {
  margin: 8px 0;
  color: #666;
}

.export-btn {
  background: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 8px;
}

.export-btn:hover {
  background: #1976D2;
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
