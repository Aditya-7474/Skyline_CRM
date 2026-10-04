const API_BASE = (localStorage.getItem('skyline_api_base') || 'http://localhost:8001') + '/api';
const TOKEN_KEY = 'sv_token';
const USER_KEY = 'sv_user';
const WORKFLOW = ['Admin', 'Employee', 'Agent'];
const EMPLOYEE = ['Admin', 'Employee'];
const ADMIN = ['Admin'];

const NAV = [
  ['dashboard', 'Dashboard', '▦', null],
  ['enquiry', 'Client Enquiry', '+', WORKFLOW],
  ['leads', 'Lead Management', '♙', WORKFLOW],
  ['followups', 'Follow-up', '◷', WORKFLOW],
  ['property', 'Property', '⌂', ADMIN],
  ['site-visits', 'Site Visit', '⌖', ADMIN],
  ['bookings', 'Booking', '▣', WORKFLOW],
  ['sales', 'Sales', '↗', WORKFLOW],
  ['documents', 'Documents', '▤', ADMIN],
  ['loans', 'Loans', '▥', ADMIN],
  ['agreements', 'Agreements', '▤', ADMIN],
  ['payments', 'Payments', '₹', ADMIN],
  ['vendor-payments', 'Vendor Payment', '◈', EMPLOYEE],
  ['possession', 'Possession', '⚿', ADMIN],
  ['support', 'Customer Support', '◌', ADMIN],
  ['vendors', 'Vendors', '▱', ADMIN],
  ['purchase-orders', 'Purchase Orders', '▤', ADMIN],
  ['employees', 'Employees & Payroll', '♟', ADMIN],
  ['attendance', 'Attendance', '✓', ADMIN],
  ['salary-runs', 'Payroll', '₹', ADMIN],
  ['petty-cash', 'Petty Cash', '▧', EMPLOYEE],
  ['accounts', 'Accounts', '⌁', ADMIN],
  ['settings', 'Settings', '⚙', ADMIN],
  ['register', 'Create User', '+', ADMIN]
];

const LABELS = Object.fromEntries(NAV.map(([p, l]) => [p, l]));
const ROLE_MENUS = {
  Admin: [
    { route: 'dashboard', label: 'Dashboard', icon: '▦' },
    { route: 'enquiry', label: 'Client Enquiry', icon: '+' },
    { route: 'leads', label: 'Lead Management', icon: '♙' },
    { route: 'followups', label: 'Follow-up', icon: '◷' },
    { route: 'site-visits', label: 'Site Visit Management', icon: '⌖' },
    { route: 'bookings', label: 'Booking', icon: '▣' },
    { route: 'sales', label: 'Sales', icon: '↗' },
    { route: 'agreements', label: 'Agreements', icon: '▤' },
    { route: 'payments', label: 'Payments', icon: '₹' },
    { route: 'property', label: 'Property', icon: '⌂' },
    { route: 'documents', label: 'Documents', icon: '▤' },
    { route: 'loans', label: 'Loans', icon: '▥' },
    { route: 'possession', label: 'Possession', icon: '⚿' },
    { route: 'support', label: 'Customer Support', icon: '◌' },
    { route: 'vendors', label: 'Vendors', icon: '▱' },
    { route: 'vendor-payments', label: 'Vendor Payment', icon: '◈' },
    { route: 'purchase-orders', label: 'Purchase Orders', icon: '▤' },
    { route: 'petty-cash', label: 'Petty Cash', icon: '▧' },
    { route: 'employees', label: 'Employee Management', icon: '♟' },
    { route: 'attendance', label: 'Attendance', icon: '✓' },
    { route: 'salary-runs', label: 'Salary / Payroll', icon: '₹' },
    { route: 'accounts', label: 'Accounts & Finance', icon: '⌁' },
    { route: 'register', label: 'Create User', icon: '+' },
    { metric: 'total_employees', label: 'Total Employees', icon: '♟' },
    { metric: 'total_agents', label: 'Total Agents', icon: '♙' }
  ],
  Employee: [
    { route: 'dashboard', label: 'Dashboard', icon: '▦' },
    { route: 'enquiry', label: 'Client Enquiry', icon: '+' },
    { route: 'leads', label: 'Lead Management', icon: '♙' },
    { route: 'followups', label: 'Follow-up', icon: '◷' },
    { route: 'site-visits', label: 'Site Visit Management', icon: '⌖' },
    { route: 'bookings', label: 'Booking Management', icon: '▣' },
    { route: 'sales', label: 'Sales / Negotiation', icon: '↗' },
    { route: 'agreements', label: 'Agreement Management', icon: '▤' },
    { route: 'payments', label: 'Payment Management', icon: '₹' },
    { route: 'property', label: 'Unit / Property', icon: '⌂' },
    { route: 'documents', label: 'Document Management', icon: '▤' },
    { route: 'loans', label: 'Loan Management', icon: '▥' },
    { route: 'support', label: 'Customer Support', icon: '◌' },
    { action: 'logout', label: 'Logout', icon: '↪' }
  ],
  Agent: [
    { route: 'dashboard', label: 'Dashboard', icon: '▦' },
    { route: 'enquiry', label: 'Client Enquiry', icon: '+' },
    { route: 'followups', label: 'Follow-up', icon: '◷' },
    { route: 'leads', label: 'Lead Management', icon: '♙' },
    { route: 'site-visits', label: 'Site Visit Management', icon: '⌖' },
    { route: 'bookings', label: 'Booking Management', icon: '▣' },
    { route: 'sales', label: 'Sales / Negotiation', icon: '↗' },
    { route: 'property', label: 'Unit / Property', icon: '⌂' },
    { route: 'documents', label: 'Document Management', icon: '▤' },
    { action: 'logout', label: 'Login/Logout', icon: '↪' }
  ]
};
function menuItems() { return ROLE_MENUS[role()] || []; }
function allowedRoute(route) { return menuItems().some(item => item.route === route); }
function defaultRoute() { return role() === 'Employee' ? 'enquiry' : 'dashboard'; }
const PATHS = {
  dashboard: '/', enquiry: '/enquiry', leads: '/leads', followups: '/followups',
  property: '/property', 'site-visits': '/site-visits', bookings: '/bookings',
  sales: '/sales', documents: '/documents', loans: '/loans', agreements: '/agreements',
  payments: '/payments', 'vendor-payments': '/vendor-payments', possession: '/possession',
  support: '/support', vendors: '/vendors', 'purchase-orders': '/purchase-orders',
  employees: '/employees', attendance: '/attendance', 'salary-runs': '/salary-runs',
  'petty-cash': '/petty-cash', accounts: '/accounts', settings: '/settings',
  register: '/register'
};

const PAGE_FILES = {
  login: 'login.html', dashboard: 'dashboard.html', enquiry: 'enquiry.html', leads: 'leads.html',
  followups: 'followups.html', 'site-visits': 'site-visits.html', bookings: 'bookings.html',
  sales: 'sales.html', agreements: 'agreements.html', payments: 'payments.html',
  'vendor-payments': 'vendor-payments.html', property: 'property.html', documents: 'documents.html',
  loans: 'loans.html', possession: 'possession.html', support: 'support.html', vendors: 'vendors.html',
  'purchase-orders': 'purchase-orders.html', employees: 'employees.html', attendance: 'attendance.html',
  'salary-runs': 'salary-runs.html', 'petty-cash': 'petty-cash.html', accounts: 'accounts.html', settings: 'settings.html', register: 'register.html'
};

function pageFile(route) { return PAGE_FILES[route] || 'index.html'; }
function pageUrl(route, query = '') { return `${pageFile(route)}#/${route}${query ? `?view=${query}` : ''}`; }

const CONFIGS = ['1 BHK', '2 BHK', '3 BHK', '4 BHK'];
const STATUSES = ['New Lead', 'Qualified', 'Not Interested', 'Future Prospect', 'Wrong Number', 'Duplicate', 'Converted'];
const SOURCES = ['Website', 'Facebook', 'Instagram', 'Google Ads', 'WhatsApp', 'Phone Call', 'Walk-in', 'Referral', 'Property Portal'];

const MODULES = {
  enquiry: {
    endpoint: '/leads', title: 'Client Enquiry', badge: 'Enquiry Intake',
    hero: 'Every conversation starts here.', copy: 'Log every incoming enquiry and hand it to the right agent.',
    label: 'Enquiries',
    fields: [['name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['email', 'Email', 'email'], ['project_name', 'Project', 'text'], ['city', 'City', 'text'], ['budget', 'Budget (₹)', 'number'], ['configuration', 'Configuration', 'select', false, CONFIGS], ['source', 'Lead Source', 'select', true, SOURCES], ['assigned_to', 'Assigned Executive', 'text'], ['interested_property', 'Interested Property', 'text'], ['status', 'Status', 'select', false, STATUSES], ['notes', 'Notes', 'textarea']],
    search: ['name', 'customer_name', 'mobile', 'email'], filters: [['source', 'All Sources', SOURCES], ['status', 'All Statuses', STATUSES]]
  },
  leads: {
    endpoint: '/leads', title: 'Lead Management', badge: 'Qualification Desk',
    hero: 'Sort the ready from the maybes.', copy: 'Capture qualification details after the first conversation to focus on the highest-intent buyers.',
    label: 'Leads',
    fields: [['name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['email', 'Email', 'email'], ['status', 'Qualification Status', 'select', true, STATUSES], ['budget', 'Budget (₹)', 'number'], ['loan_required', 'Loan Required', 'checkbox'], ['preferred_location', 'Preferred Location', 'text'], ['flat_type', 'Preferred Flat Type', 'select', false, CONFIGS], ['timeline', 'Purchase Timeline', 'select', false, ['Immediate', '1-3 Months', '3-6 Months', '6+ Months']], ['purpose', 'Purpose', 'select', false, ['Self Use', 'Investment']], ['notes', 'Qualification Notes', 'textarea']],
    search: ['name', 'customer_name', 'mobile', 'email'], filters: [['status', 'All Statuses', STATUSES]]
  },
  followups: {
    endpoint: '/followups', title: 'Follow-ups', badge: 'Nudge Queue',
    hero: 'Never let a hot lead go cold.', copy: 'Schedule the next touchpoint and keep the whole team aligned.',
    label: 'Follow-ups',
    fields: [['lead_id', 'Existing Customer (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['type', 'Follow-up Type', 'select', true, ['Phone Call', 'WhatsApp', 'Email', 'SMS', 'Meeting', 'Video Call']], ['date', 'Date', 'date', true], ['time', 'Time', 'time', true], ['next_date', 'Next Follow-up Date', 'date'], ['status', 'Status', 'select', false, ['Scheduled', 'Completed', 'Cancelled']], ['remarks', 'Remarks', 'textarea']],
    search: ['lead_name', 'customer_name', 'mobile', 'customer_email', 'type', 'remarks']
  },
  property: {
    endpoint: '/units', title: 'Property', badge: 'Inventory Desk',
    hero: 'Every unit, one source of truth.', copy: 'Track availability and price across every wing and floor.',
    label: 'Units',
    fields: [['unit_number', 'Unit Number', 'text', true], ['flat_type', 'Flat Type', 'select', true, CONFIGS], ['wing', 'Wing', 'text', true], ['floor', 'Floor', 'number', true], ['carpet_area', 'Carpet Area (sqft)', 'number', true], ['built_up_area', 'Built-up Area (sqft)', 'number', true], ['price', 'Price (₹)', 'number', true], ['parking', 'Parking', 'text'], ['amenities', 'Amenities', 'textarea'], ['facing', 'Facing', 'select', false, ['East', 'West', 'North', 'South']], ['status', 'Status', 'select', false, ['Available', 'Sold', 'Blocked']]],
    search: ['unit_number'], filters: [['flat_type', 'All Types', CONFIGS], ['status', 'All Statuses', ['Available', 'Sold', 'Blocked']]]
  },
  'site-visits': {
    endpoint: '/site-visits', title: 'Site Visit', badge: 'Field Activity',
    hero: 'Upcoming site visits, ready for the road.', copy: 'Plan every visit and keep the field team aligned.',
    label: 'Site Visits',
    fields: [['lead_id', 'Existing Customer (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['project_name', 'Project', 'text', true], ['visit_date', 'Visit Date', 'date', true], ['visit_time', 'Visit Time', 'time'], ['pickup_required', 'Pickup Required', 'checkbox'], ['executive_name', 'Executive', 'text'], ['status', 'Status', 'select', false, ['Scheduled', 'Visited', 'Rescheduled', 'No Show', 'Cancelled']], ['feedback', 'Customer Feedback', 'textarea']],
    search: ['lead_name', 'customer_name', 'mobile', 'customer_email', 'project_name', 'status'], filters: [['status', 'All Records', ['Scheduled', 'Completed', 'Rescheduled', 'Cancelled']]]
  },
  bookings: {
    endpoint: '/bookings', title: 'Booking', badge: 'Deal Desk',
    hero: 'Sealed with a signature.', copy: 'Record confirmed bookings and let inventory flip Sold automatically.',
    label: 'Bookings',
    fields: [['lead_id', 'Existing Customer (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['project_name', 'Project', 'text'], ['unit_id', 'Unit', 'lookup', true], ['unit_number', 'Unit Number', 'readonly'], ['flat_type', 'Flat Type', 'readonly'], ['floor', 'Floor', 'readonly'], ['booking_amount', 'Booking Amount (₹)', 'number', true], ['total_price', 'Total Price (₹)', 'number', true], ['booking_date', 'Booking Date', 'date', true], ['status', 'Status', 'select', false, ['Pending', 'Confirmed', 'Cancelled']]],
    search: ['customer_name', 'lead_name', 'mobile', 'customer_email', 'project_name', 'unit_number'], filters: [['status', 'All Statuses', ['Pending', 'Confirmed', 'Cancelled']]]
  },
  sales: {
    endpoint: '/negotiations', title: 'Sales', badge: 'Negotiation Desk',
    hero: 'Every rupee of discount, tracked.', copy: 'Keep offers, approvals, and remarks in one place.',
    label: 'Sales Entries',
    fields: [['lead_id', 'Existing Customer (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['project_name', 'Project', 'text'], ['unit_id', 'Unit', 'lookup', true], ['unit_number', 'Unit Number', 'readonly'], ['offered_price', 'Offered Price (₹)', 'number', true], ['discount', 'Discount (₹)', 'number'], ['special_offer', 'Special Offer', 'text'], ['payment_mode', 'Payment Mode', 'select', false, ['Cash', 'Cheque', 'NEFT', 'RTGS', 'UPI']], ['payment_reference', 'Payment Reference', 'text'], ['approval_status', 'Management Approval', 'select', false, ['Pending', 'Approved', 'Rejected']], ['remarks', 'Remarks', 'textarea']],
    search: ['customer_name', 'lead_name', 'mobile', 'customer_email', 'project_name', 'unit_number'], filters: [['approval_status', 'All Approvals', ['Pending', 'Approved', 'Rejected']]]
  },
  documents: {
    endpoint: '/documents', title: 'Document & Loan', badge: 'Compliance Desk',
    hero: 'Six documents. One checklist. Zero missed.', copy: 'Track every customer document and loan milestone.',
    label: 'Documents',
    fields: [['customer_id', 'Customer Lead', 'lookup', true], ['customer_name', 'Customer Name', 'readonly'], ['pan', 'PAN Card', 'document-upload'], ['aadhaar', 'Aadhaar', 'document-upload'], ['passport_photo', 'Passport Photo', 'document-upload'], ['address_proof', 'Address Proof', 'document-upload'], ['income_proof', 'Income Proof', 'document-upload'], ['bank_statement', 'Bank Statement', 'document-upload'], ['remarks', 'Remarks', 'textarea']],
    search: ['customer_name', 'mobile']
  },
  loans: {
    endpoint: '/loans', title: 'Loans', badge: 'Finance Desk',
    hero: 'Loan milestones, clearly tracked.', copy: 'Monitor sanction status, amounts, and customer finance records.',
    label: 'Loans',
    fields: [['customer_id', 'Customer Lead', 'lookup', true], ['customer_name', 'Customer Name', 'readonly'], ['bank_name', 'Bank Name', 'text', true], ['loan_amount', 'Loan Amount (₹)', 'number', true], ['emi', 'EMI (₹)', 'number'], ['sanction_date', 'Sanction Date', 'date'], ['status', 'Status', 'select', false, ['Applied', 'Under Review', 'Sanctioned', 'Rejected']], ['remarks', 'Remarks', 'textarea']],
    search: ['customer_name', 'bank_name'], filters: [['status', 'All Statuses', ['Applied', 'Under Review', 'Sanctioned', 'Rejected']]]
  },
  agreements: {
    endpoint: '/agreements', title: 'Agreements', badge: 'Legal Desk',
    hero: 'Every agreement, ready for review.', copy: 'Track agreement numbers, dates, and execution status.',
    label: 'Agreements',
    fields: [['booking_id', 'Existing Booking (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['project_name', 'Project', 'text'], ['unit_number', 'Unit Number', 'text'], ['agreement_number', 'Agreement Number', 'text', true], ['agreement_date', 'Agreement Date', 'date', true], ['stamp_duty', 'Stamp Duty (₹)', 'number'], ['registration_date', 'Registration Date', 'date'], ['registration_number', 'Registration Number', 'text'], ['agreement_value', 'Agreement Value (₹)', 'number'], ['status', 'Status', 'select', false, ['Draft', 'Under Review', 'Executed', 'Cancelled']], ['remarks', 'Remarks', 'textarea']],
    search: ['customer_name', 'lead_name', 'mobile', 'customer_email', 'project_name', 'agreement_number', 'unit_number'], filters: [['status', 'All Statuses', ['Draft', 'Under Review', 'Executed', 'Cancelled']]]
  },
  payments: {
    endpoint: '/payments', title: 'Payments', badge: 'Collections Desk',
    hero: 'Every installment, receipted.', copy: 'Record collections against every confirmed booking.',
    label: 'Payments',
    fields: [['booking_id', 'Existing Booking (optional)', 'lookup'], ['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text', true], ['customer_email', 'Email', 'email'], ['project_name', 'Project', 'text'], ['unit_number', 'Unit Number', 'text'], ['installment_type', 'Installment Type', 'select', true, ['Booking Amount', 'Agreement Payment', 'Slab Payment', 'Final Payment']], ['total_cost', 'Total Cost (₹)', 'number'], ['amount', 'Amount (₹)', 'number', true], ['pending_amount', 'Pending Amount (₹)', 'number'], ['due_date', 'Due Date', 'date'], ['payment_date', 'Payment Date', 'date', true], ['mode', 'Mode', 'select', true, ['Cash', 'UPI', 'Bank Transfer', 'Cheque']], ['reference', 'Reference / UTR', 'text'], ['remarks', 'Remarks', 'textarea']],
    search: ['customer_name', 'lead_name', 'mobile', 'customer_email', 'project_name', 'unit_number', 'reference']
  },
  'vendor-payments': {
    endpoint: '/vendor-payments', title: 'Vendor Payment', badge: 'Accounts Desk',
    hero: 'Release every vendor payment with a trail.', copy: 'Track bills, bank references, and paid amounts.',
    label: 'Vendor Payments',
    fields: [['bill_id', 'Existing Vendor Bill (optional)', 'lookup'], ['bill_number', 'Bill Number', 'text'], ['vendor_name', 'Vendor Name', 'text', true], ['vendor_mobile', 'Vendor Mobile', 'text'], ['payment_date', 'Payment Date', 'date', true], ['mode', 'Payment Mode', 'select', true, ['Cash', 'Cheque', 'NEFT', 'RTGS', 'UPI']], ['paid_amount', 'Paid Amount (₹)', 'number', true], ['utr', 'UTR / Reference', 'text'], ['bank', 'Bank', 'text'], ['remarks', 'Remarks', 'textarea']],
    search: ['vendor_name', 'vendor_mobile', 'bill_number', 'utr']
  },
  possession: {
    endpoint: '/possessions', title: 'Possession', badge: 'Handover Desk',
    hero: 'The last mile before the key ring.', copy: 'Close the loop from booking to handover.',
    label: 'Possessions',
    fields: [['booking_id', 'Booking', 'lookup', true], ['customer_name', 'Customer Name', 'readonly'], ['unit_number', 'Unit Number', 'readonly'], ['final_inspection', 'Final Inspection', 'checkbox'], ['utility_connection', 'Utility Connection', 'checkbox'], ['key_handover', 'Key Handover', 'checkbox'], ['possession_letter', 'Possession Letter', 'checkbox'], ['status', 'Status', 'select', false, ['Pending', 'Ready', 'Completed']], ['handover_date', 'Handover Date', 'date']],
    search: ['customer_name', 'unit_number']
  },
  support: {
    endpoint: '/support', title: 'Customer Support', badge: 'Resolution Desk',
    hero: 'Every complaint has a name and a resolver.', copy: 'Keep customer support visible from intake to resolution.',
    label: 'Support Tickets',
    fields: [['customer_name', 'Customer Name', 'text', true], ['mobile', 'Mobile', 'text'], ['email', 'Email', 'email'], ['unit_number', 'Unit Number', 'text'], ['type', 'Ticket Type', 'select', true, ['Maintenance', 'Complaint', 'Service Request', 'Documentation', 'Referral']], ['subject', 'Subject', 'text', true], ['description', 'Description', 'textarea'], ['assigned_to', 'Assigned To', 'text'], ['status', 'Status', 'select', false, ['Open', 'In Progress', 'Resolved']], ['resolution', 'Resolution', 'textarea']],
    search: ['customer_name', 'subject'], filters: [['status', 'All Statuses', ['Open', 'In Progress', 'Resolved']]]
  },
  vendors: {
    endpoint: '/vendors', title: 'Vendors', badge: 'Partner Desk',
    hero: 'Every partner, on tap.', copy: 'Manage vendor records and contacts.',
    label: 'Vendors',
    fields: [['name', 'Vendor Name', 'text', true], ['company', 'Company Name', 'text', true], ['category', 'Category', 'select', true, ['Construction', 'Legal', 'Marketing', 'Maintenance', 'Other']], ['gst', 'GST Number', 'text'], ['pan', 'PAN Number', 'text'], ['contact_person', 'Contact Person', 'text'], ['mobile', 'Mobile', 'text', true], ['email', 'Email', 'email'], ['bank_account', 'Bank Account', 'text'], ['ifsc', 'IFSC Code', 'text'], ['address', 'Address', 'textarea']],
    search: ['name', 'company', 'mobile'], filters: [['category', 'All Categories', ['Construction', 'Legal', 'Marketing', 'Maintenance', 'Other']]]
  },
  'purchase-orders': {
    endpoint: '/purchase-orders', title: 'Purchase Orders', badge: 'Procurement Desk',
    hero: 'Every order, tracked.', copy: 'Keep vendor purchases and approvals organized.',
    label: 'Purchase Orders',
    fields: [['vendor_id', 'Vendor', 'text'], ['vendor_name', 'Vendor Name', 'text', true], ['po_number', 'PO Number', 'text', true], ['po_date', 'PO Date', 'date', true], ['amount', 'Amount (₹)', 'number', true], ['status', 'Status', 'select', false, ['Draft', 'Approved', 'Received', 'Cancelled']], ['description', 'Description', 'textarea'], ['remarks', 'Remarks', 'textarea']],
    search: ['vendor_name', 'po_number']
  },
  employees: {
    endpoint: '/employees', title: 'Employees & Payroll', badge: 'People Desk',
    hero: 'Every team member, one record.', copy: 'Keep employee details and payroll inputs current.',
    label: 'Employees',
    fields: [['employee_code', 'Employee Code', 'text', true], ['name', 'Name', 'text', true], ['department', 'Department', 'text', true], ['designation', 'Designation', 'text', true], ['mobile', 'Mobile', 'text', true], ['email', 'Email', 'email'], ['doj', 'Date of Joining', 'date', true], ['basic_salary', 'Basic Salary', 'number', true], ['hra', 'HRA', 'number'], ['allowances', 'Allowances', 'number'], ['bank_account', 'Bank Account', 'text'], ['ifsc', 'IFSC', 'text'], ['pan', 'PAN', 'text'], ['aadhaar', 'Aadhaar', 'text'], ['pf_number', 'PF Number', 'text'], ['esic_number', 'ESIC', 'text']],
    search: ['name', 'employee_code', 'department']
  },
  attendance: {
    endpoint: '/attendance', title: 'Attendance', badge: 'People Desk',
    hero: 'Attendance without the spreadsheet chase.', copy: 'Record daily attendance and keep the employee trail clear.',
    label: 'Attendance',
    fields: [['employee_id', 'Employee', 'text', true], ['employee_name', 'Employee Name', 'text', true], ['date', 'Date', 'date', true], ['status', 'Status', 'select', true, ['Present', 'Absent', 'Half Day', 'Leave']], ['check_in', 'Check In', 'time'], ['check_out', 'Check Out', 'time'], ['remarks', 'Remarks', 'textarea']],
    search: ['employee_name', 'employee_id'], filters: [['status', 'All Statuses', ['Present', 'Absent', 'Half Day', 'Leave']]]
  },
  'petty-cash': {
    endpoint: '/petty-cash', title: 'Petty Cash', badge: 'Accounts Desk',
    hero: 'Every rupee, receipted.', copy: 'Capture small expenses with a clean approval trail.',
    label: 'Petty Cash',
    fields: [['date', 'Date', 'date', true], ['voucher_number', 'Voucher #', 'text', true], ['category', 'Category', 'select', true, ['Travel', 'Office', 'Refreshments', 'Maintenance', 'Other']], ['amount', 'Amount (₹)', 'number', true], ['payment_mode', 'Payment Mode', 'select', false, ['Cash', 'UPI', 'Card', 'Bank']], ['employee_name', 'Employee Name', 'text'], ['requested_by', 'Requested By', 'text'], ['approved_by', 'Approved By', 'text'], ['description', 'Description', 'textarea', true], ['remarks', 'Remarks', 'textarea']],
    search: ['voucher_number', 'category', 'description'], filters: [['category', 'All Categories', ['Travel', 'Office', 'Refreshments', 'Maintenance', 'Other']]]
  },
  'vendor-bills': {
    endpoint: '/vendor-bills', title: 'Vendor Bills', badge: 'Accounts Desk',
    hero: 'Outstanding bills, visible.', copy: 'Monitor vendor bill balances and due dates.',
    label: 'Vendor Bills',
    fields: [['vendor_id', 'Vendor', 'text'], ['vendor_name', 'Vendor Name', 'text', true], ['bill_number', 'Bill Number', 'text', true], ['bill_date', 'Bill Date', 'date', true], ['bill_amount', 'Bill Amount (₹)', 'number', true], ['paid_amount', 'Paid Amount (₹)', 'number'], ['balance', 'Balance (₹)', 'number'], ['status', 'Status', 'select', false, ['Pending', 'Partially Paid', 'Fully Paid']], ['due_date', 'Due Date', 'date'], ['remarks', 'Remarks', 'textarea']],
    search: ['vendor_name', 'bill_number']
  },
  'salary-runs': {
    endpoint: '/salary-runs', title: 'Payroll', badge: 'Payroll Desk',
    hero: 'Payroll with a clear trail.', copy: 'View and update monthly salary runs.',
    label: 'Salary Runs',
    fields: [['employee_id', 'Employee ID', 'text', true], ['employee_name', 'Employee Name', 'text', true], ['month', 'Month', 'month', true], ['basic', 'Basic', 'number'], ['gross', 'Gross', 'number'], ['deductions', 'Deductions', 'number'], ['net', 'Net', 'number'], ['status', 'Status', 'select', false, ['Pending', 'Paid']], ['paid_date', 'Paid Date', 'date']],
    search: ['employee_name', 'month'], filters: [['status', 'All Statuses', ['Pending', 'Paid']]]
  }
};

const state = {
  route: 'login',
  items: [],
  selected: null,
  form: {},
  search: '',
  filters: {},
  mobile: false,
  profile: false,
  kpis: {},
  refs: { leads: [], units: [], bookings: [], vendorBills: [] }
};

const user = () => { try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch { return null; } };
const token = () => localStorage.getItem(TOKEN_KEY);
const role = () => user()?.role || '';
const can = allowed => !allowed || allowed.includes(role());
const esc = v => String(v ?? '').replace(/[&<>'"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[c]));
const compact = v => String(v ?? '').toLowerCase().replace(/[^a-z0-9]/g, '');
const digits = v => String(v ?? '').replace(/\D/g, '').replace(/^0+(?!$)/, '');
const money = v => v == null || v === '' ? '₹ 0' : '₹ ' + Number(v).toLocaleString('en-IN');
const initials = n => String(n || 'SV').split(' ').map(x => x[0]).slice(0, 2).join('').toUpperCase();

function toast(message, error = false) {
  const root = document.getElementById('toast-root');
  if (!root) return;
  const el = document.createElement('div');
  el.className = 'toast' + (error ? ' error' : '');
  el.textContent = message;
  root.append(el);
  setTimeout(() => el.remove(), 3300);
}

function path() {
  const hash = location.hash.replace(/^#\/?/, '').split('?')[0];
  if (hash) return hash;
  const filename = location.pathname.split('/').pop().toLowerCase();
  const pageRoute = Object.entries(PAGE_FILES).find(([, file]) => file === filename)?.[0];
  return pageRoute || (token() ? defaultRoute() : 'login');
}

function go(p, query = '') { location.href = pageUrl(p, query); }

function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  state.profile = false;
  history.replaceState(null, '', 'login.html');
  location.replace('login.html');
}

async function api(pathname, options = {}) {
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token() ? { Authorization: `Bearer ${token()}` } : {})
  };
  const res = await fetch(API_BASE + pathname, { ...options, headers });
  let data = null;
  try { data = await res.json(); } catch { }
  if (!res.ok) {
    if (res.status === 401) logout();
    throw new Error(data?.detail || data?.message || data?.error || `Request failed (${res.status})`);
  }
  return data;
}

function pageName() { return LABELS[state.route] || 'Dashboard'; }

function displayDate(value) {
  const date = toInputDate(value);
  if (!date) return '';
  const [y, m, d] = date.split('-');
  return y && m && d ? `${d}-${m}-${y}` : date;
}

function toInputDate(value) {
  const raw = String(value ?? '').trim();
  if (!raw) return '';
  if (/^\d{4}-\d{2}-\d{2}/.test(raw)) return raw.slice(0, 10);
  const match = raw.match(/^(\d{1,2})[-/](\d{1,2})[-/](\d{4})$/);
  if (!match) return raw;
  return `${match[3]}-${match[2].padStart(2, '0')}-${match[1].padStart(2, '0')}`;
}

function leadName(lead) {
  return lead?.customer_name || lead?.name || '';
}

function leadLabel(lead) {
  return [leadName(lead), lead?.mobile, lead?.email].filter(Boolean).join(' · ');
}

function unitLabel(unit) {
  return [unit?.unit_number, unit?.flat_type, unit?.status, unit?.price ? money(unit.price) : ''].filter(Boolean).join(' · ');
}

function bookingLabel(booking) {
  return [booking?.customer_name || booking?.lead_name, booking?.unit_number ? 'Unit ' + booking.unit_number : '', booking?.status].filter(Boolean).join(' · ');
}

function billLabel(bill) {
  return [bill?.bill_number, bill?.vendor_name, bill?.balance != null ? 'Balance ' + money(bill.balance) : '', bill?.status].filter(Boolean).join(' · ');
}

function activeUnitIds() {
  return new Set(state.refs.bookings.filter(b => b.status !== 'Cancelled').map(b => String(b.unit_id)));
}

function currentSelectedUnitId() {
  return state.selected?.unit_id ? String(state.selected.unit_id) : '';
}

function lookupOptions(name) {
  if (name === 'lead_id' || name === 'customer_id') return state.refs.leads.map(item => ({ value: item.id, label: leadLabel(item) }));
  if (name === 'unit_id') {
    const blocked = state.route === 'bookings' ? activeUnitIds() : new Set();
    const current = currentSelectedUnitId();
    return state.refs.units
      .filter(item => !blocked.has(String(item.id)) || String(item.id) === current)
      .map(item => ({ value: item.id, label: unitLabel(item) }));
  }
  if (name === 'booking_id') return state.refs.bookings.filter(b => b.status !== 'Cancelled').map(item => ({ value: item.id, label: bookingLabel(item) }));
  if (name === 'bill_id') return state.refs.vendorBills.map(item => ({ value: item.id, label: billLabel(item) }));
  return [];
}

function findByValue(rows, value, keys) {
  const input = String(value ?? '').trim();
  if (!input) return null;
  const exact = rows.find(row => keys.some(key => compact(row[key]) === compact(input)));
  if (exact) return exact;
  const byDigits = rows.find(row => keys.some(key => digits(row[key]) && digits(row[key]) === digits(input)));
  if (byDigits) return byDigits;
  const contains = rows.filter(row => keys.some(key => compact(row[key]).includes(compact(input))));
  return contains.length === 1 ? contains[0] : null;
}

function findLead(value) { return findByValue(state.refs.leads, value, ['id', 'name', 'customer_name', 'mobile', 'email']); }
function findUnit(value) { return findByValue(state.refs.units, value, ['id', 'unit_number']); }
function findBooking(value) { return findByValue(state.refs.bookings, value, ['id', 'customer_name', 'lead_name', 'mobile', 'unit_number']); }
function findBill(value) { return findByValue(state.refs.vendorBills, value, ['id', 'bill_number', 'vendor_name']); }

async function loadReferences(routeName) {
  const needsLeads = ['followups', 'site-visits', 'bookings', 'sales', 'documents', 'loans'].includes(routeName);
  const needsUnits = ['bookings', 'sales'].includes(routeName);
  const needsBookings = ['bookings', 'agreements', 'payments', 'possession'].includes(routeName);
  const needsBills = routeName === 'vendor-payments';
  const tasks = [];
  if (needsLeads) tasks.push(api('/leads').then(v => { state.refs.leads = v; }));
  if (needsUnits) tasks.push(api('/units').then(v => { state.refs.units = v; }));
  if (needsBookings) tasks.push(api('/bookings').then(v => { state.refs.bookings = v; }));
  if (needsBills) tasks.push(api('/vendor-bills').then(v => { state.refs.vendorBills = v; }).catch(() => { state.refs.vendorBills = []; }));
  await Promise.all(tasks);
}

function fieldHtml(f, value) {
  const [name, label, type = '', required = false, options = []] = f;
  const v = value == null ? '' : value;
  if (type === 'document-upload') {
    const filename = state.form[`${name}_filename`] || (v && !['Pending', 'Received', 'Verified'].includes(String(v)) ? v : '');
    const uploaded = Boolean(state.form[`${name}_path`] || filename);
    const download = uploaded && state.selected?.id ? `<a class="btn btn-secondary" href="${esc(documentFileUrl(state.selected.id, name))}" target="_blank" rel="noopener">View / Download</a>` : '';
    return `<div class="document-upload"><div class="document-upload-row"><input class="input" name="${name}_file" type="file" accept=".pdf,.png,.jpg,.jpeg,.webp,.doc,.docx" data-document-file="${name}"><button class="btn btn-secondary" type="button" data-upload-document="${name}">Upload</button>${download}</div><div class="document-upload-status ${uploaded ? 'success' : ''}" data-upload-status="${name}">${uploaded ? `Uploaded: ${esc(filename)}` : 'No file uploaded yet.'}</div></div>`;
  }
  if (type === 'lookup') {
    const opts = lookupOptions(name);
    return `<select class="select" name="${name}" ${required ? 'required' : ''}><option value="">Select ${esc(label)}</option>${opts.map(o => `<option value="${esc(o.value)}" ${String(v) === String(o.value) ? 'selected' : ''}>${esc(o.label)}</option>`).join('')}</select>`;
  }
  if (type === 'select') return `<select class="select" name="${name}" ${required ? 'required' : ''}><option value="">Select ${esc(label)}</option>${options.map(o => `<option value="${esc(o)}" ${String(v) === String(o) ? 'selected' : ''}>${esc(o)}</option>`).join('')}</select>`;
  if (type === 'textarea') return `<textarea class="textarea" name="${name}" rows="3" ${required ? 'required' : ''}>${esc(v)}</textarea>`;
  if (type === 'checkbox') return `<label class="check"><input type="checkbox" name="${name}" ${v ? 'checked' : ''}> Yes</label>`;
  if (type === 'readonly') return `<input class="input" name="${name}" type="text" value="${esc(v)}" readonly>`;
  const safeValue = type === 'date' ? toInputDate(v) : v;
  return `<input class="input" name="${name}" type="${type || 'text'}" value="${esc(safeValue)}" ${required ? 'required' : ''}>`;
}

function documentFileUrl(id, type) {
  return `${API_BASE}/documents/${encodeURIComponent(id)}/file/${encodeURIComponent(type)}`;
}

function recordName(item, index) {
  if (state.route === 'followups') return `Lead: ${item.lead_name || item.customer_name || `Follow-up ${index + 1}`}`;
  if (state.route === 'site-visits') return `Customer: ${item.customer_name || item.lead_name || `Site Visit ${index + 1}`}`;
  if (state.route === 'bookings') return `Customer: ${item.customer_name || item.lead_name || `Booking ${index + 1}`}`;
  if (state.route === 'sales') return `Customer: ${item.customer_name || item.lead_name || `Sales Entry ${index + 1}`}`;
  if (state.route === 'agreements') return `Customer: ${item.customer_name || item.lead_name || `Agreement ${index + 1}`}`;
  if (state.route === 'payments') return `Customer: ${item.customer_name || item.lead_name || `Payment ${index + 1}`}`;
  if (state.route === 'vendor-payments') return `Vendor: ${item.vendor_name || `Vendor Payment ${index + 1}`}`;
  return item.name || item.customer_name || item.vendor_name || item.employee_name || item.subject || item.voucher_number || item.unit_number || `Record ${index + 1}`;
}

function recordMeta(item) {
  if (state.route === 'followups') return [`Customer: ${item.customer_name || 'Not recorded'}`, `Mobile: ${item.mobile || 'Not recorded'}`, `Type: ${item.type || 'Not recorded'}`, `Date: ${displayDate(item.next_date || item.date) || 'Not recorded'}`].join(' · ');
  if (state.route === 'site-visits') return [`Mobile: ${item.mobile || item.customer_mobile || 'Not recorded'}`, `Project: ${item.project_name || 'Not recorded'}`, `Visit Date: ${displayDate(item.visit_date) || 'Not recorded'}`].join(' · ');
  if (state.route === 'bookings') return [`Mobile: ${item.mobile || item.customer_mobile || 'Not recorded'}`, item.unit_number ? `Unit: ${item.unit_number}` : '', item.project_name ? `Project: ${item.project_name}` : ''].filter(Boolean).join(' · ');
  if (state.route === 'sales') return [`Mobile: ${item.mobile || item.customer_mobile || 'Not recorded'}`, item.unit_number ? `Unit: ${item.unit_number}` : '', item.offered_price != null ? `Offered: ${money(item.offered_price)}` : '', item.discount != null ? `Discount: ${money(item.discount)}` : ''].filter(Boolean).join(' · ');
  if (state.route === 'agreements') return [`Mobile: ${item.mobile || item.customer_mobile || 'Not recorded'}`, `Agreement: ${item.agreement_number || ''}`, `Date: ${displayDate(item.agreement_date)}`].filter(x => !x.endsWith(': ')).join(' · ');
  if (state.route === 'payments') return [`Mobile: ${item.mobile || item.customer_mobile || 'Not recorded'}`, `Installment: ${item.installment_type || ''}`, item.amount != null ? `Amount: ${money(item.amount)}` : '', `Date: ${displayDate(item.payment_date)}`, `Mode: ${item.mode || ''}`].filter(x => !x.endsWith(': ')).join(' · ');
  if (state.route === 'vendor-payments') return [`Vendor: ${item.vendor_name || ''}`, `Mobile: ${item.vendor_mobile || 'Not recorded'}`, item.paid_amount != null ? `Amount: ${money(item.paid_amount)}` : '', `Date: ${displayDate(item.payment_date)}`, `Mode: ${item.mode || ''}`, `Reference: ${item.utr || ''}`].filter(x => !x.endsWith(': ')).join(' · ');
  return [item.mobile, item.email, item.unit_number, item.vendor_name, item.employee_code, item.category, item.source].filter(Boolean).join(' · ');
}

function recordFoot(item, index) {
  const right = item.booking_amount != null ? money(item.booking_amount)
    : item.total_price != null ? money(item.total_price)
      : item.amount != null ? money(item.amount)
        : item.paid_amount != null ? money(item.paid_amount)
          : item.budget != null ? money(item.budget)
            : displayDate(item.booking_date || item.payment_date || item.date || item.visit_date || item.created_at);
  return `<span>S.No ${index + 1}</span><span>${esc(right || '')}</span>`;
}

function statusClass(v) {
  return ['Qualified', 'Confirmed', 'Completed', 'Approved', 'Paid', 'Available', 'Resolved', 'Fully Paid', 'Converted', 'Executed'].includes(v) ? 'success'
    : ['Pending', 'Scheduled', 'In Progress', 'Partially Paid', 'Draft', 'Under Review'].includes(v) ? 'warning'
      : ['Cancelled', 'Rejected', 'Not Interested', 'Absent'].includes(v) ? 'danger' : '';
}

function statusText(item) {
  if (state.route === 'followups') return item.status || 'Scheduled';
  if (state.route === 'site-visits') return item.status || 'Scheduled';
  if (state.route === 'sales') return item.approval_status || item.status || 'Pending';
  if (state.route === 'vendor-payments') return item.status || 'Recorded';
  return item.status || item.approval_status || item.category || item.source || item.mode || 'Record';
}

function loginView() {
  return `<div class="login-page"><div class="login-grid"><section class="login-brand"><div><div class="brand"><div class="brand-mark">▥</div><div><div class="brand-title">SKYLINE REAL ESTATE CRM</div><div class="brand-sub">Smart Property Management and Lead Tracking Platform</div></div></div><span class="pill" style="margin-top:56px">FIELD-READY COMMAND CENTER</span><h1>Everything from the first call to the key handover.</h1><p>Sales pipeline, inventory, bookings, payments and back-office - all under one roof.</p></div><div class="muted">Smart Property Management and Lead Tracking Platform</div></section><section class="login-form"><h2>Sign in to your workspace</h2><p>Sign in to your Skyline Real Estate CRM workspace.</p><form id="login-form"><label class="login-label">Email</label><input id="login-email" class="input" type="email" value="admin@saivandan.com" required><label class="login-label">Password</label><input id="login-password" class="input" type="password" value="Admin@123" required><div id="login-error" class="login-error"></div><button class="btn btn-primary" style="width:100%;margin-top:16px;font-size:15px;padding:14px">Sign in</button></form><div class="section-label" style="margin-top:34px">Demo credentials</div><div class="demo-grid">${[['Admin', 'admin@saivandan.com', 'Admin@123'], ['Employee', 'employee@saivandan.com', 'Employee@123'], ['Agent', 'agent@saivandan.com', 'Agent@123']].map(x => `<button class="demo" data-demo="${x[0]}" data-email="${x[1]}" data-password="${x[2]}"><strong>${x[0]}</strong><span>${x[1]}</span><span class="muted">${x[2]}</span></button>`).join('')}</div><div style="text-align:center;margin-top:30px;color:#94a3b8">New to Skyline? <a href="#/register" class="text-blue" style="font-weight:700">Create an account</a></div></section></div></div>`;
}

function registerView() {
  return `<div class="login-page"><div class="login-grid" style="max-width:920px"><section class="login-brand"><div><div class="brand"><div class="brand-mark">▥</div><div><div class="brand-title">SKYLINE REAL ESTATE CRM</div><div class="brand-sub">Smart Property Management and Lead Tracking Platform</div></div></div><span class="pill" style="margin-top:56px">TEAM ONBOARDING</span><h1>Bring every property conversation into focus.</h1><p>Create an Employee or Agent workspace account and start working from one command center.</p></div><div class="muted">Existing user? <a href="#/login" class="text-blue">Sign in</a></div></section><section class="login-form"><h2>Create an account</h2><p>${role() === 'Admin' ? 'Create a workspace user with the required role.' : 'Create an Employee or Agent account. Admin accounts are created by an existing Admin.'}</p><form id="register-form"><div class="form-grid"><div><label class="login-label">Full Name</label><input class="input" name="fullName" required></div><div><label class="login-label">Email Address</label><input class="input" name="email" type="email" required></div><div><label class="login-label">Mobile Number</label><input class="input" name="mobile" pattern="[6-9][0-9]{9}" required></div><div><label class="login-label">Role</label><select class="select" name="role"><option>Agent</option><option>Employee</option>${role() === 'Admin' ? '<option>Admin</option>' : ''}</select></div><div><label class="login-label">Password</label><input class="input" name="password" type="password" minlength="8" required></div><div><label class="login-label">Confirm Password</label><input class="input" name="confirmPassword" type="password" minlength="8" required></div></div><div id="register-error" class="login-error"></div><div class="register-actions"><button class="btn btn-primary" type="submit">Create account</button><button class="btn btn-secondary" type="button" id="register-logout">↪ &nbsp; ${token() ? 'Sign out' : 'Back to login'}</button></div></form></section></div></div>`;
}

function sidebar() {
  const links = menuItems().map(item => {
    if (item.action === 'logout') return `<button class="nav-link nav-action" data-menu-logout><span class="nav-icon">${item.icon}</span><span>${item.label}</span></button>`;
    if (item.metric) return `<div class="nav-link nav-metric"><span class="nav-icon">${item.icon}</span><span>${item.label}</span><strong class="nav-count">${esc(state.kpis[item.metric] ?? '')}</strong></div>`;
    const href = pageUrl(item.route, item.query || '');
    return `<a class="nav-link ${state.route === item.route ? 'active' : ''}" href="${href}"><span class="nav-icon">${item.icon}</span><span>${item.label}</span></a>`;
  }).join('');
  const footer = menuItems().some(item => item.action === 'logout') ? '' : '<div class="sidebar-actions"><button class="logout-action" id="sidebar-logout">↪ <span>Sign out</span></button></div>';
  return `<aside class="sidebar"><div class="brand"><div class="brand-mark">▥</div><div><div class="brand-title">SKYLINE REAL ESTATE CRM</div><div class="brand-sub">Sales Command Center</div></div></div><div class="nav-caption">${esc(role())} Workspace</div><nav class="nav">${links}</nav>${footer}</aside>`;
}

function topbar() {
  const u = user();
  const crumb = pageName();
  return `<header class="topbar"><button class="mobile-menu" id="mobile-menu">☰</button><div class="crumbs"><span>Dashboard</span><span>›</span>${crumb === 'Dashboard' ? '' : '<strong>' + esc(crumb) + '</strong>'}</div><div class="top-spacer"></div><div class="search"><span>⌕</span><input id="global-search" placeholder="Search current page..."></div><button class="circle-btn" id="theme-btn" title="Toggle theme">☼</button><button class="circle-btn" title="Notifications">♧</button><button class="top-logout" id="top-logout" title="Sign out">↪ <span>Sign out</span></button><div class="profile-wrap"><button class="profile-btn" id="profile-btn"><span class="avatar">${initials(u?.name)}</span><span class="role-label">${esc((u?.role || '').toLowerCase())}</span></button>${state.profile ? `<div class="profile-menu"><div class="profile-info"><div class="profile-name">${esc(u?.name)}</div><div class="profile-email">${esc(u?.email)}</div><div class="profile-role">${esc(u?.role)}</div></div><button id="logout-btn">↪ &nbsp; Sign out</button></div>` : ''}</div></header>`;
}

function shell(content) {
  return `<div class="shell">${sidebar()}<div class="main-wrap">${topbar()}<main class="content">${content}</main><footer class="footer"><span>SKYLINE REAL ESTATE CRM</span><span>Smart Property Management and Lead Tracking Platform</span></footer></div>${state.mobile ? `<div class="mobile-nav" id="mobile-nav"><div class="sidebar">${sidebar().replace('<aside class="sidebar">', '').replace('</aside>', '')}</div></div>` : ''}</div>`;
}

async function dashboardView() {
  let d = { k: {}, p: { pipeline: {}, sources: {} }, t: { trend: [], sources: [] }, v: [], f: [] };
  try {
    [d.k, d.p, d.t, d.v, d.f] = await Promise.all([
      api('/dashboard/kpis'), api('/dashboard/pipeline'), api('/dashboard/monthly-trends'),
      api('/site-visits?status=Scheduled'), api('/followups?due=today')
    ]);
  } catch (e) { toast(e.message, true); }
  state.kpis = d.k || {};
  const kpis = [['open_visits', 'Open Visits', '⌖', 'Scheduled this week'], ['hot_listings', 'Hot Listings', '⌂', 'Ready for field demo'], ['bookings_in_hand', 'Bookings in Hand', '▣', 'In negotiation'], ['closure_rate', 'Closure Rate', '↗', 'Agent performance']];
  const workspace = role() === 'Admin' ? 'Admin Command Center' : role() === 'Agent' ? 'Agent Workspace' : 'Employee Console';
  return `<div class="hero"><span class="pill">${workspace}</span><h1>Field-ready dashboard for site visits and deal closures.</h1><p>Focus on high-value listings, scheduled visits, and booked opportunities while staying mobile-friendly.</p><div class="hero-actions"><a class="btn btn-primary" href="${pageUrl('bookings')}">▣ &nbsp; Bookings</a></div></div><div class="grid kpi-grid">${kpis.map(([key, label, icon, cap]) => `<div class="card"><div class="kpi-top"><div class="kpi-icon">${icon}</div><div class="kpi-label">${label}</div></div><div class="kpi-value">${esc(d.k[key] ?? 0)}${key === 'closure_rate' ? '%' : ''}</div><div class="kpi-caption">${cap}</div></div>`).join('')}</div>${role() === 'Admin' ? `<div class="grid two-grid"><div class="card"><div class="kpi-top"><div class="kpi-icon">♟</div><div class="kpi-label">Total Employees</div></div><div class="kpi-value">${d.k.total_employees ?? 0}</div></div><div class="card"><div class="kpi-top"><div class="kpi-icon">♙</div><div class="kpi-label">Total Agents</div></div><div class="kpi-value">${d.k.total_agents ?? 0}</div></div></div>` : ''}<div class="grid three-grid"><div class="card"><div class="section-label">Live Trend</div><div class="card-title">Bookings & Collections · Last 6 Months</div><div class="chart">${(d.t.trend || []).map(x => `<div class="bar-col"><div class="bar" style="height:${Math.max(5, Math.min(90, (Number(x.bookings || 0) * 22)))}%" title="${x.bookings} bookings"></div><span class="bar-label">${esc(x.label)}</span></div>`).join('')}</div></div><div class="card"><div class="section-label">Source Mix</div><div class="card-title">Leads by Channel</div>${Object.entries(d.p.sources || {}).map(([key, val]) => `<div style="margin-top:15px"><div style="display:flex;justify-content:space-between;font-size:11px"><span class="muted">${esc(key)}</span><b>${val}</b></div><div class="progress-line"><span style="width:${Math.min(100, Number(val) * 20)}%"></span></div></div>`).join('') || '<div class="empty">No source data yet.</div>'}</div></div><div class="grid two-grid"><div class="card"><div class="section-label">Field Activity</div><div class="card-title">Upcoming Site Visits</div>${(d.v || []).slice(0, 6).map(x => `<div class="list-row"><div class="list-main"><div class="list-name">${esc(x.lead_name || x.customer_name || 'Client')}</div><div class="list-meta">${esc(displayDate(x.visit_date))} · ${esc(x.visit_time || '')} · ${esc(x.mobile || x.executive_name || 'Unassigned')}</div></div><span class="status">${esc(x.status || 'Scheduled')}</span></div>`).join('') || '<div class="empty">No upcoming visits.</div>'}</div><div class="card"><div class="section-label">Nudge Queue</div><div class="card-title">Follow-ups Due Today</div>${(d.f || []).slice(0, 6).map(x => `<div class="list-row"><div class="list-main"><div class="list-name">${esc(x.lead_name || x.customer_name || x.type || 'Follow-up')}</div><div class="list-meta">${esc(x.mobile || '')} · ${esc(displayDate(x.next_date || x.date))} · ${esc(x.remarks || 'No remarks')}</div></div><span class="status warning">${esc(x.time || '')}</span></div>`).join('') || '<div class="empty">Nothing due today.</div>'}</div></div>`;
}

function renderModule(config, items) {
  const filtered = items.filter(item => {
    const q = state.search.toLowerCase();
    const qOk = !q || (config.search || ['name']).some(k => String(item[k] ?? '').toLowerCase().includes(q));
    const fOk = Object.entries(state.filters).every(([k, v]) => !v || String(item[k] ?? '') === v);
    return qOk && fOk;
  });
  const selected = state.selected;
  const form = state.form;
  return `<div class="page-head"><div><div class="page-title">${config.title}</div><div class="page-sub">${config.copy}</div></div><div class="split-actions"><button class="btn btn-secondary" id="export-btn">⇩ Export</button><button class="btn btn-primary" id="new-btn">+ Add New</button></div></div><div class="hero"><span class="pill">${config.badge}</span><h1>${config.hero}</h1><p>${config.copy}</p></div><div class="module-grid" style="margin-top:16px"><section class="card list-card"><div class="section-label">Field Activity</div><div class="list-head"><div class="card-title">${config.label} <span class="muted" style="font-size:12px;font-weight:500">(${filtered.length})</span></div></div><div class="toolbar"><input id="module-search" class="input" placeholder="Search ${config.label.toLowerCase()}..." value="${esc(state.search)}"></div>${config.filters ? `<div class="toolbar">${config.filters.map(([k, l, opts]) => `<select class="select filter" data-filter="${k}"><option value="">${l}</option>${opts.map(o => `<option value="${esc(o)}" ${state.filters[k] === o ? 'selected' : ''}>${esc(o)}</option>`).join('')}</select>`).join('')}</div>` : ''}<div class="record-list">${filtered.map((item, index) => `<button class="record ${selected?.id === item.id ? 'active' : ''}" data-record-index="${state.items.indexOf(item)}"><div class="record-title"><span>${esc(recordName(item, index))}</span><span class="status ${statusClass(statusText(item))}">${esc(statusText(item))}</span></div><div class="record-meta">${esc(recordMeta(item))}</div><div class="record-foot">${recordFoot(item, index)}</div></button>`).join('') || '<div class="empty">No records yet. Click Add New to create one.</div>'}</div></section><section class="card form-card"><div class="section-label">Form Panel</div><div class="list-head"><div><div class="card-title">${selected ? 'Edit ' + esc(recordName(selected, 0)) : 'Create a new record'}</div><div class="page-sub">${selected?.id ? 'Update the selected record.' : 'Enter the details below.'}</div></div><div class="split-actions">${selected ? '<button class="btn btn-danger" id="delete-btn">Delete</button>' : ''}<button class="btn btn-primary" id="save-btn">${selected ? 'Update' : 'Create'}</button></div></div><form id="record-form" class="form-grid">${config.fields.map(f => `<div class="form-field ${f[2] === 'textarea' ? 'full' : ''}"><label>${esc(f[1])}${f[3] ? '<span style="color:#f87171"> *</span>' : ''}</label>${fieldHtml(f, form[f[0]])}</div>`).join('')}</form></section></div>`;
}

async function moduleView(key) {
  const config = MODULES[key] || MODULES.leads;
  try {
    await loadReferences(key);
    state.items = await api(config.endpoint);
  } catch (e) {
    state.items = [];
    toast(e.message, true);
  }
  return renderModule(config, state.items);
}

async function settingsView() {
  let users = [];
  try { users = await api('/auth/users'); } catch (e) { toast(e.message, true); }
  return `<div class="page-head"><div><div class="page-title">Settings</div><div class="page-sub">Manage users, roles and permissions for the CRM.</div></div></div><div class="card"><div class="section-label">User Management</div><div class="card-title">Workspace Users</div><div class="table-wrap" style="margin-top:16px"><table class="table"><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr></thead><tbody>${users.map(u => `<tr><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td><span class="status">${esc(u.role)}</span></td><td>${esc(u.status)}</td><td><button class="btn btn-secondary user-edit" data-user="${esc(u.id)}">Edit</button></td></tr>`).join('')}</tbody></table></div></div><div class="card" style="margin-top:16px"><div class="section-label">Create User</div><form id="user-form" class="form-grid" style="margin-top:14px"><div class="form-field"><label>Name</label><input class="input" name="name" required></div><div class="form-field"><label>Email</label><input class="input" name="email" type="email" required></div><div class="form-field"><label>Phone</label><input class="input" name="phone"></div><div class="form-field"><label>Role</label><select class="select" name="role"><option>Employee</option><option>Agent</option><option>Admin</option></select></div><div class="form-field"><label>Password</label><input class="input" name="password" type="password" minlength="8" required></div><div class="form-field" style="align-self:end"><button class="btn btn-primary">Create User</button></div></form></div>`;
}

async function accountsView() {
  let data = {};
  try { data = await api('/dashboard/accounts'); } catch (e) { toast(e.message, true); }
  return `<div class="page-head"><div><div class="page-title">Accounts & Reports</div><div class="page-sub">Collections, receivables and operating outflow in one view.</div></div></div><div class="grid kpi-grid">${[['total_collected', 'Total Collected'], ['receivables', 'Receivables'], ['vendor_outstanding', 'Vendor Outstanding'], ['petty_spent', 'Petty Cash Spent'], ['salary_paid', 'Salary Paid'], ['salary_pending', 'Salary Pending']].map(([k, l]) => `<div class="card"><div class="section-label">${l}</div><div class="kpi-value" style="font-size:27px;margin-top:14px">${money(data[k] || 0)}</div></div>`).join('')}</div><div class="card" style="margin-top:16px"><div class="section-label">Profit Summary</div><div class="card-title" style="font-size:30px;margin-top:15px">${money(data.profit_summary || 0)}</div><div class="page-sub">Collected less vendor, salary and petty cash outflow.</div></div>`;
}

async function render() {
  state.route = path();
  state.profile = false;
  const protectedRoute = state.route !== 'login' && state.route !== 'register';
  if (protectedRoute && !token()) { logout(); return; }
  if (state.route === 'login') {
    document.getElementById('app').innerHTML = loginView();
    bindLogin();
    return;
  }
  if (state.route === 'register') {
    document.getElementById('app').innerHTML = registerView();
    bindRegister();
    return;
  }
  if (!allowedRoute(state.route)) {
    go(defaultRoute());
    return;
  }
  let content = '';
  if (state.route === 'dashboard') content = await dashboardView();
  else if (state.route === 'settings') content = await settingsView();
  else if (state.route === 'accounts') content = await accountsView();
  else content = await moduleView(state.route);
  document.getElementById('app').innerHTML = shell(content);
  bindShell();
  if (MODULES[state.route]) bindModule(MODULES[state.route]);
  if (state.route === 'settings') bindSettings();
}

function bindLogin() {
  document.querySelectorAll('[data-demo]').forEach(b => {
    b.onclick = () => {
      document.getElementById('login-email').value = b.dataset.email;
      document.getElementById('login-password').value = b.dataset.password;
    };
  });
  document.getElementById('login-form').onsubmit = async e => {
    e.preventDefault();
    const err = document.getElementById('login-error');
    err.style.display = 'none';
    try {
      const data = await api('/auth/login', { method: 'POST', body: JSON.stringify({ email: document.getElementById('login-email').value, password: document.getElementById('login-password').value }) });
      localStorage.setItem(TOKEN_KEY, data.token);
      localStorage.setItem(USER_KEY, JSON.stringify(data.user));
      toast('Signed in successfully');
      go(defaultRoute());
    } catch (x) {
      err.textContent = x.message;
      err.style.display = 'block';
    }
  };
}

function bindRegister() {
  document.getElementById('register-form').onsubmit = async e => {
    e.preventDefault();
    const form = new FormData(e.target);
    const data = Object.fromEntries(form.entries());
    const err = document.getElementById('register-error');
    err.style.display = 'none';
    if (data.password !== data.confirmPassword) {
      err.textContent = 'Passwords do not match';
      err.style.display = 'block';
      return;
    }
    try {
      const admin = role() === 'Admin' && token();
      const endpoint = admin ? '/auth/register' : '/auth/register-public';
      const payload = admin ? { name: data.fullName, email: data.email, phone: data.mobile, password: data.password, role: data.role } : data;
      await api(endpoint, { method: 'POST', body: JSON.stringify(payload) });
      toast(admin ? 'User created successfully.' : 'Account created. Please sign in.');
      go(admin ? 'dashboard' : 'login');
    } catch (x) {
      err.textContent = x.message;
      err.style.display = 'block';
    }
  };
  document.getElementById('register-logout')?.addEventListener('click', () => token() ? logout() : location.replace('login.html'));
}

function bindShell() {
  document.getElementById('profile-btn')?.addEventListener('click', () => { state.profile = !state.profile; render(); });
  document.getElementById('logout-btn')?.addEventListener('click', logout);
  document.getElementById('sidebar-logout')?.addEventListener('click', logout);
  document.querySelector('[data-menu-logout]')?.addEventListener('click', logout);
  document.getElementById('top-logout')?.addEventListener('click', logout);
  document.getElementById('mobile-menu')?.addEventListener('click', () => { state.mobile = true; render(); });
  document.getElementById('mobile-nav')?.addEventListener('click', e => { if (e.target.id === 'mobile-nav') { state.mobile = false; render(); } });
  document.getElementById('theme-btn')?.addEventListener('click', () => document.body.classList.toggle('light-mode'));
  document.getElementById('global-search')?.addEventListener('keydown', e => {
    if (e.key === 'Enter' && MODULES[state.route]) {
      state.search = e.target.value;
      const moduleSearch = document.getElementById('module-search');
      if (moduleSearch) moduleSearch.value = state.search;
      renderModuleInPlace();
    }
  });
}

function renderModuleInPlace() {
  const box = document.querySelector('.module-grid');
  if (!box) return;
  const config = MODULES[state.route];
  box.outerHTML = renderModule(config, state.items).match(/<div class="module-grid"[\s\S]*<\/div>$/)?.[0] || box.outerHTML;
  bindModule(config);
}

function setFormValue(form, name, value, overwrite = true) {
  const el = form.querySelector(`[name="${name}"]`);
  if (!el || value == null || value === '') return;
  if (!overwrite && el.value) return;
  el.value = value;
}

function applyRelationDefaults(form, changedName) {
  const value = form.elements[changedName]?.value;
  if (changedName === 'lead_id' || changedName === 'customer_id') {
    const lead = findLead(value);
    if (!lead) return;
    setFormValue(form, 'lead_id', lead.id);
    setFormValue(form, 'customer_id', lead.id);
    setFormValue(form, 'lead_name', leadName(lead));
    setFormValue(form, 'customer_name', leadName(lead));
    setFormValue(form, 'mobile', lead.mobile);
    setFormValue(form, 'customer_mobile', lead.mobile);
    setFormValue(form, 'email', lead.email);
    setFormValue(form, 'customer_email', lead.email);
  }
  if (changedName === 'unit_id') {
    const unit = findUnit(value);
    if (!unit) return;
    setFormValue(form, 'unit_id', unit.id);
    setFormValue(form, 'unit_number', unit.unit_number);
    setFormValue(form, 'unit_name', unit.unit_number);
    setFormValue(form, 'flat_type', unit.flat_type);
    setFormValue(form, 'floor', unit.floor);
    setFormValue(form, 'total_price', unit.price, false);
  }
  if (changedName === 'booking_id') {
    const booking = findBooking(value);
    if (!booking) return;
    setFormValue(form, 'booking_id', booking.id);
    setFormValue(form, 'customer_name', booking.customer_name);
    setFormValue(form, 'lead_name', booking.lead_name);
    setFormValue(form, 'mobile', booking.mobile);
    setFormValue(form, 'customer_mobile', booking.customer_mobile || booking.mobile);
    setFormValue(form, 'customer_email', booking.customer_email);
    setFormValue(form, 'project_name', booking.project_name);
    setFormValue(form, 'unit_number', booking.unit_number);
    setFormValue(form, 'unit_name', booking.unit_name || booking.unit_number);
  }
  if (changedName === 'bill_id') {
    const bill = findBill(value);
    if (!bill) return;
    setFormValue(form, 'bill_id', bill.id);
    setFormValue(form, 'bill_number', bill.bill_number);
    setFormValue(form, 'vendor_name', bill.vendor_name);
    setFormValue(form, 'paid_amount', bill.balance, false);
  }
}

function hydratePayload(body) {
  if (!body.mobile && body.customer_mobile) body.mobile = body.customer_mobile;
  if (!body.customer_mobile && body.mobile) body.customer_mobile = body.mobile;
  if (!body.email && body.customer_email) body.email = body.customer_email;
  if (!body.customer_email && body.email) body.customer_email = body.email;
  if (!body.unit_number && body.unit_name) body.unit_number = body.unit_name;
  if (!body.unit_name && body.unit_number) body.unit_name = body.unit_number;
  const lead = findLead(body.lead_id || body.customer_id);
  if (lead) {
    if (body.lead_id !== undefined) body.lead_id = lead.id;
    if (body.customer_id !== undefined) body.customer_id = lead.id;
    body.lead_name = leadName(lead);
    body.customer_name = body.customer_name || leadName(lead);
    body.mobile = body.mobile || lead.mobile;
    body.customer_mobile = body.customer_mobile || lead.mobile;
    body.customer_email = body.customer_email || lead.email;
  }
  const unit = findUnit(body.unit_id || body.unit_number);
  if (unit) {
    body.unit_id = unit.id;
    body.unit_number = unit.unit_number;
    body.flat_type = body.flat_type || unit.flat_type;
    body.floor = body.floor || unit.floor;
  }
  const booking = findBooking(body.booking_id);
  if (booking) {
    body.booking_id = booking.id;
    body.lead_id = booking.lead_id;
    body.customer_name = booking.customer_name || body.customer_name;
    body.lead_name = booking.lead_name || body.lead_name;
    body.mobile = body.mobile || booking.mobile;
    body.customer_mobile = body.customer_mobile || booking.customer_mobile || booking.mobile;
    body.customer_email = body.customer_email || booking.customer_email;
    body.project_name = body.project_name || booking.project_name;
    body.unit_number = booking.unit_number || body.unit_number;
    body.unit_name = body.unit_name || booking.unit_name || booking.unit_number;
  }
  const bill = findBill(body.bill_id);
  if (bill) {
    body.bill_id = bill.id;
    body.bill_number = bill.bill_number;
    body.vendor_id = bill.vendor_id;
    body.vendor_name = bill.vendor_name || body.vendor_name;
  }
  return body;
}

function bindModule(config) {
  document.getElementById('new-btn')?.addEventListener('click', () => { state.selected = null; state.form = {}; render(); });
  document.getElementById('module-search')?.addEventListener('input', e => { state.search = e.target.value; renderModuleInPlace(); });
  document.querySelectorAll('.filter').forEach(s => s.addEventListener('change', e => { state.filters[e.target.dataset.filter] = e.target.value; renderModuleInPlace(); }));
  document.querySelectorAll('[data-record-index]').forEach(b => b.addEventListener('click', () => {
    state.selected = state.items[Number(b.dataset.recordIndex)];
    state.form = { ...(state.selected || {}) };
    render();
  }));
  const form = document.getElementById('record-form');
  form?.addEventListener('change', e => {
    if (['lead_id', 'customer_id', 'unit_id', 'booking_id', 'bill_id'].includes(e.target.name)) applyRelationDefaults(form, e.target.name);
  });
  document.getElementById('save-btn')?.addEventListener('click', saveRecord);
  document.getElementById('delete-btn')?.addEventListener('click', deleteRecord);
  document.querySelectorAll('[data-upload-document]').forEach(button => button.addEventListener('click', () => uploadDocument(button.dataset.uploadDocument)));
  document.getElementById('export-btn')?.addEventListener('click', () => {
    const rows = state.items.map((item, index) => ({
      serial: index + 1,
      name: recordName(item, index),
      details: recordMeta(item),
      status: statusText(item)
    }));
    const csv = ['serial,name,details,status', ...rows.map(row => Object.values(row).map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = config.label.toLowerCase().replace(/\s+/g, '-') + '.csv';
    a.click();
  });
}

function formBodyFromConfig(form, config) {
  const fd = new FormData(form);
  const body = {};
  config.fields.forEach(f => {
    const [name, , type] = f;
    if (type === 'document-upload') return;
    if (type === 'checkbox') body[name] = fd.get(name) === 'on';
    else if (fd.get(name) !== null && fd.get(name) !== '') body[name] = type === 'number' ? Number(fd.get(name)) : type === 'date' ? toInputDate(fd.get(name)) : fd.get(name);
  });
  return body;
}

async function uploadDocument(type) {
  if (state.route !== 'documents') return;
  const input = document.querySelector(`[data-document-file="${type}"]`);
  const file = input?.files?.[0];
  if (!file) { toast('Choose a file first.', true); return; }
  try {
    let documentId = state.selected?.id;
    if (!documentId) {
      const form = document.getElementById('record-form');
      if (!form.reportValidity()) return;
      let body = hydratePayload(formBodyFromConfig(form, MODULES.documents));
      const created = await api('/documents', { method: 'POST', body: JSON.stringify(body) });
      documentId = created.id;
      state.selected = created;
    }
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`${API_BASE}/documents/${encodeURIComponent(documentId)}/upload?type=${encodeURIComponent(type)}`, { method: 'POST', headers: token() ? { Authorization: `Bearer ${token()}` } : {}, body: formData });
    let data = null;
    try { data = await response.json(); } catch { }
    if (!response.ok) throw new Error(data?.detail || data?.message || data?.error || `Upload failed (${response.status})`);
    const documents = await api('/documents');
    const updated = documents.find(item => String(item.id) === String(documentId));
    state.selected = updated || state.selected;
    state.form = { ...(state.selected || {}) };
    toast(`${data?.filename || file.name} uploaded successfully.`);
    await render();
  } catch (error) {
    toast(error.message || 'Document upload failed.', true);
  }
}

async function saveRecord(e) {
  e.preventDefault();
  const config = MODULES[state.route];
  const form = document.getElementById('record-form');
  if (!form.reportValidity()) return;
  let body = formBodyFromConfig(form, config);
  if ((state.route === 'leads' || state.route === 'enquiry') && body.status) body.qualification_status = body.status;
  body = hydratePayload(body);
  try {
    if (state.selected?.id) await api(`${config.endpoint}/${state.selected.id}`, { method: 'PUT', body: JSON.stringify(body) });
    else await api(config.endpoint, { method: 'POST', body: JSON.stringify(body) });
    toast(state.selected ? 'Updated' : 'Created');
    state.selected = null;
    state.form = {};
    await render();
  } catch (x) {
    toast(x.message, true);
  }
}

async function deleteRecord() {
  if (!state.selected?.id || !confirm('Delete this record?')) return;
  try {
    await api(`${MODULES[state.route].endpoint}/${state.selected.id}`, { method: 'DELETE' });
    toast('Deleted');
    state.selected = null;
    state.form = {};
    await render();
  } catch (x) {
    toast(x.message, true);
  }
}

function bindSettings() {
  document.getElementById('user-form')?.addEventListener('submit', async e => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    try {
      await api('/auth/register', { method: 'POST', body: JSON.stringify(data) });
      toast('User created');
      await render();
    } catch (x) {
      toast(x.message, true);
    }
  });
}

window.addEventListener('hashchange', () => {
  state.search = '';
  state.filters = {};
  state.selected = null;
  state.form = {};
  render();
});
window.addEventListener('DOMContentLoaded', render);
if (document.readyState !== 'loading') render();
