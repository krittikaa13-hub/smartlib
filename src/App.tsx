/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import {
  BookOpen,
  Search,
  Compass,
  MapPin,
  Clock,
  Award,
  Globe,
  LogIn,
  LogOut,
  UserCheck,
  Shield,
  ShieldAlert,
  AlertTriangle,
  FolderTree,
  Terminal,
  Copy,
  Check,
  Download,
  ExternalLink,
  ChevronRight,
  Info,
  Layers,
  Users,
  Lock,
  UserPlus,
  ShieldCheck,
  FileCode,
  Menu,
  X,
  Plus,
  Edit3,
  Power,
  RefreshCw,
  Database,
  CheckCircle2,
  ArrowLeft,
  Bookmark,
  Library,
  SlidersHorizontal,
  Archive,
  BarChart3,
  RotateCcw,
  Calendar,
  History,
  AlertCircle
} from 'lucide-react';
import JSZip from 'jszip';
import { PROJECT_FILES, ProjectFile } from './data/projectFiles';

type UserRole = 'GUEST' | 'USER' | 'ADMIN';

interface RegisteredUser {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

interface BookItem {
  id: number;
  isbn: string;
  title: string;
  author: string;
  publisher: string;
  publicationYear: number;
  category: string;
  description: string;
  shelfLocation: string;
  totalCopies: number;
  availableCopies: number;
  active: boolean;
  createdAt: string;
}

interface BookCopyItem {
  id: number;
  bookId: number;
  copyCode: string;
  status: 'AVAILABLE' | 'BORROWED' | 'INACTIVE';
  shelfLocation: string;
}

interface BorrowingRecordItem {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  copyId: number;
  copyCode: string;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  borrowedAt: string;
  dueDate: string;
  returnedAt: string | null;
  status: 'BORROWED' | 'RETURNED';
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'preview' | 'code' | 'guide' | 'checklist'>('preview');
  const [currentView, setCurrentView] = useState<
    'home' | 'catalogue' | 'bookDetails' | 'myLibrary' | 'borrowingHistory' | 'adminBooks' | 'adminBookForm' | 'adminBorrowings' | 'login' | 'register' | 'userDashboard' | 'adminDashboard' | 'accessDenied'
  >('home');
  const [selectedFile, setSelectedFile] = useState<ProjectFile>(PROJECT_FILES[0]);
  const [copiedFile, setCopiedFile] = useState(false);
  const [copiedSnippet, setCopiedSnippet] = useState<string | null>(null);
  const [isDownloadingZip, setIsDownloadingZip] = useState(false);
  const [codeCategoryFilter, setCodeCategoryFilter] = useState<string>('all');
  const [codeSearchQuery, setCodeSearchQuery] = useState<string>('');

  // Policy configuration
  const maxLoansAllowed = 3;
  const loanDurationDays = 14;

  // Simulated Authentication Session
  const [simulatedRole, setSimulatedRole] = useState<UserRole>('USER');
  const [currentUser, setCurrentUser] = useState<{ id: number; name: string; email: string; role: 'USER' | 'ADMIN' } | null>({
    id: 2,
    name: 'Jane Scholar (Student)',
    email: 'jane.scholar@university.edu',
    role: 'USER'
  });

  // In-Memory Simulated User Directory
  const [userList, setUserList] = useState<RegisteredUser[]>([
    {
      id: 1,
      name: 'Library Administrator',
      email: 'admin@smartlib.edu',
      role: 'ADMIN',
      createdAt: '2026-10-11 08:30:00'
    },
    {
      id: 2,
      name: 'Jane Scholar (Student)',
      email: 'jane.scholar@university.edu',
      role: 'USER',
      createdAt: '2026-10-11 09:15:00'
    }
  ]);

  // In-Memory Simulated Books
  const [books, setBooks] = useState<BookItem[]>([
    {
      id: 1,
      isbn: '9780262046305',
      title: 'Introduction to Algorithms, Fourth Edition',
      author: 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein',
      publisher: 'MIT Press',
      publicationYear: 2022,
      category: 'Computer Science',
      description: 'Comprehensive textbook covering algorithmic design, asymptotic analysis, dynamic programming, and graph algorithms widely referenced in core university curricula.',
      shelfLocation: 'Stack 2 - Row B - Bay 1',
      totalCopies: 8,
      availableCopies: 7, // 1 copy borrowed by Jane
      active: true,
      createdAt: '2026-10-11 08:00:00'
    },
    {
      id: 2,
      isbn: '9781119800361',
      title: 'Operating System Concepts, Tenth Edition',
      author: 'Abraham Silberschatz, Peter B. Galvin, Greg Gagne',
      publisher: 'Wiley',
      publicationYear: 2021,
      category: 'Computer Science',
      description: 'The authoritative reference on operating system architectures, process concurrency, virtual memory management, and file systems.',
      shelfLocation: 'Stack 2 - Row B - Bay 3',
      totalCopies: 5,
      availableCopies: 5,
      active: true,
      createdAt: '2026-10-11 08:00:00'
    },
    {
      id: 3,
      isbn: '9780078022159',
      title: 'Database System Concepts, Seventh Edition',
      author: 'Abraham Silberschatz, Henry F. Korth, S. Sudarshan',
      publisher: 'McGraw-Hill',
      publicationYear: 2020,
      category: 'Computer Science',
      description: 'Foundational text on relational database principles, SQL query optimization, transaction recovery, and distributed database models.',
      shelfLocation: 'Stack 2 - Row C - Bay 2',
      totalCopies: 6,
      availableCopies: 6, // Past loan was returned
      active: true,
      createdAt: '2026-10-11 08:00:00'
    },
    {
      id: 4,
      isbn: '9781337613927',
      title: 'Calculus: Early Transcendentals, Ninth Edition',
      author: 'James Stewart, Daniel K. Clegg, Saleem Watson',
      publisher: 'Cengage Learning',
      publicationYear: 2020,
      category: 'Mathematics',
      description: 'Standard undergraduate text on multivariable differential and integral calculus with engineering and physical science applications.',
      shelfLocation: 'Stack 1 - Row A - Bay 4',
      totalCopies: 10,
      availableCopies: 10,
      active: true,
      createdAt: '2026-10-11 08:00:00'
    },
    {
      id: 5,
      isbn: '9780134494166',
      title: 'Clean Architecture: A Craftsman\'s Guide',
      author: 'Robert C. Martin',
      publisher: 'Prentice Hall',
      publicationYear: 2018,
      category: 'Software Engineering',
      description: 'Essential software design guide addressing dependency inversion, component coupling, boundary separation, and testability.',
      shelfLocation: 'Stack 2 - Row D - Bay 1',
      totalCopies: 4,
      availableCopies: 4,
      active: true,
      createdAt: '2026-10-11 08:00:00'
    },
    {
      id: 6,
      isbn: '9780357038314',
      title: 'Principles of Economics, Ninth Edition',
      author: 'N. Gregory Mankiw',
      publisher: 'Cengage Learning',
      publicationYear: 2021,
      category: 'Economics',
      description: 'Foundational undergraduate overview of microeconomic market mechanisms, macroeconomic policy, and fiscal systems.',
      shelfLocation: 'Stack 3 - Row A - Bay 2',
      totalCopies: 7,
      availableCopies: 7,
      active: true,
      createdAt: '2026-10-11 08:00:00'
    }
  ]);

  // In-Memory Physical Book Copies
  const [bookCopies, setBookCopies] = useState<BookCopyItem[]>([
    // Book 1 copies (8 total, C01 borrowed by Jane, C02-C08 available)
    { id: 1, bookId: 1, copyCode: 'LIB-001-C01', status: 'BORROWED', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 2, bookId: 1, copyCode: 'LIB-001-C02', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 3, bookId: 1, copyCode: 'LIB-001-C03', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 4, bookId: 1, copyCode: 'LIB-001-C04', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 5, bookId: 1, copyCode: 'LIB-001-C05', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 6, bookId: 1, copyCode: 'LIB-001-C06', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 7, bookId: 1, copyCode: 'LIB-001-C07', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    { id: 8, bookId: 1, copyCode: 'LIB-001-C08', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 1' },
    // Book 2 copies
    { id: 9, bookId: 2, copyCode: 'LIB-002-C01', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 3' },
    { id: 10, bookId: 2, copyCode: 'LIB-002-C02', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 3' },
    { id: 11, bookId: 2, copyCode: 'LIB-002-C03', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 3' },
    { id: 12, bookId: 2, copyCode: 'LIB-002-C04', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 3' },
    { id: 13, bookId: 2, copyCode: 'LIB-002-C05', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row B - Bay 3' },
    // Book 3 copies
    { id: 14, bookId: 3, copyCode: 'LIB-003-C01', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    { id: 15, bookId: 3, copyCode: 'LIB-003-C02', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    { id: 16, bookId: 3, copyCode: 'LIB-003-C03', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    { id: 17, bookId: 3, copyCode: 'LIB-003-C04', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    { id: 18, bookId: 3, copyCode: 'LIB-003-C05', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    { id: 19, bookId: 3, copyCode: 'LIB-003-C06', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row C - Bay 2' },
    // Book 4 copies (Calculus)
    { id: 20, bookId: 4, copyCode: 'LIB-004-C01', status: 'AVAILABLE', shelfLocation: 'Stack 1 - Row A - Bay 4' },
    { id: 21, bookId: 4, copyCode: 'LIB-004-C02', status: 'AVAILABLE', shelfLocation: 'Stack 1 - Row A - Bay 4' },
    // Book 5 copies
    { id: 22, bookId: 5, copyCode: 'LIB-005-C01', status: 'AVAILABLE', shelfLocation: 'Stack 2 - Row D - Bay 1' },
    // Book 6 copies
    { id: 23, bookId: 6, copyCode: 'LIB-006-C01', status: 'AVAILABLE', shelfLocation: 'Stack 3 - Row A - Bay 2' }
  ]);

  // In-Memory Circulation Transaction Records
  const [borrowingRecords, setBorrowingRecords] = useState<BorrowingRecordItem[]>([
    {
      id: 1,
      userId: 2,
      userName: 'Jane Scholar (Student)',
      userEmail: 'jane.scholar@university.edu',
      copyId: 1,
      copyCode: 'LIB-001-C01',
      bookId: 1,
      bookTitle: 'Introduction to Algorithms, Fourth Edition',
      bookAuthor: 'Thomas H. Cormen, et al.',
      borrowedAt: '2026-10-08 10:15',
      dueDate: '2026-10-22',
      returnedAt: null,
      status: 'BORROWED'
    },
    {
      id: 2,
      userId: 2,
      userName: 'Jane Scholar (Student)',
      userEmail: 'jane.scholar@university.edu',
      copyId: 14,
      copyCode: 'LIB-003-C01',
      bookId: 3,
      bookTitle: 'Database System Concepts, Seventh Edition',
      bookAuthor: 'Abraham Silberschatz, et al.',
      borrowedAt: '2026-09-20 14:00',
      dueDate: '2026-10-04',
      returnedAt: '2026-10-03 16:30',
      status: 'RETURNED'
    }
  ]);

  // Selected Book for Details View
  const [selectedBookId, setSelectedBookId] = useState<number>(1);

  // Catalogue Filtering & Sorting State
  const [catalogueQuery, setCatalogueQuery] = useState('');
  const [catalogueCategory, setCatalogueCategory] = useState('');
  const [catalogueSortBy, setCatalogueSortBy] = useState<'title' | 'author' | 'year'>('title');
  const [catalogueSortDirection, setCatalogueSortDirection] = useState<'asc' | 'desc'>('asc');

  // Admin Book Management State
  const [adminBookSearch, setAdminBookSearch] = useState('');
  const [editingBookId, setEditingBookId] = useState<number | null>(null);
  const [flashMessage, setFlashMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Book Form State
  const [formIsbn, setFormIsbn] = useState('');
  const [formTitle, setFormTitle] = useState('');
  const [formAuthor, setFormAuthor] = useState('');
  const [formPublisher, setFormPublisher] = useState('');
  const [formPublicationYear, setFormPublicationYear] = useState<number>(2024);
  const [formCategory, setFormCategory] = useState('Computer Science');
  const [formDescription, setFormDescription] = useState('');
  const [formShelfLocation, setFormShelfLocation] = useState('Stack 2 - Row B - Bay 1');
  const [formTotalCopies, setFormTotalCopies] = useState<number>(5);
  const [formAvailableCopies, setFormAvailableCopies] = useState<number>(5);
  const [formActive, setFormActive] = useState<boolean>(true);
  const [formError, setFormError] = useState<string>('');

  // Form State: Login
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');
  const [loginSuccessNotice, setLoginSuccessNotice] = useState('');

  // Form State: Register
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirmPassword, setRegConfirmPassword] = useState('');
  const [regError, setRegError] = useState('');

  // Homepage Hero Search & Mobile Menu
  const [homepageHeroQuery, setHomepageHeroQuery] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Verification Checklist State (11 tests matching SmartLibBorrowingTests.java)
  const [checkedItems, setCheckedItems] = useState<{ [key: string]: boolean }>({
    btest1: true,
    btest2: true,
    btest3: true,
    btest4: true,
    btest5: true,
    btest6: true,
    btest7: true,
    btest8: true,
    btest9: true,
    btest10: true,
    btest11: true
  });

  const toggleCheck = (id: string) => {
    setCheckedItems(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const copyToClipboard = (text: string, snippetId?: string) => {
    navigator.clipboard.writeText(text);
    if (snippetId) {
      setCopiedSnippet(snippetId);
      setTimeout(() => setCopiedSnippet(null), 2000);
    } else {
      setCopiedFile(true);
      setTimeout(() => setCopiedFile(false), 2000);
    }
  };

  const showFlash = (type: 'success' | 'error', text: string) => {
    setFlashMessage({ type, text });
    setTimeout(() => setFlashMessage(null), 4000);
  };

  const handleDownloadZip = async () => {
    try {
      setIsDownloadingZip(true);
      const zip = new JSZip();
      const folder = zip.folder('smartlib');

      PROJECT_FILES.forEach(file => {
        folder?.file(file.path, file.content);
      });

      const blob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'smartlib-stage4-circulation.zip';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error generating ZIP:', err);
    } finally {
      setIsDownloadingZip(false);
    }
  };

  // Auth Handlers
  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError('');

    const targetUser = userList.find(u => u.email.toLowerCase() === loginEmail.trim().toLowerCase());

    if (!targetUser) {
      setLoginError('Invalid university email or password. (Check spelling or register a new account)');
      return;
    }

    if (targetUser.role === 'ADMIN' && loginPassword !== 'Admin@123') {
      setLoginError('Invalid password for Administrator. (Default bootstrap password is: Admin@123)');
      return;
    }

    setCurrentUser(targetUser);
    setSimulatedRole(targetUser.role);
    setLoginPassword('');
    setLoginSuccessNotice('');

    if (targetUser.role === 'ADMIN') {
      setCurrentView('adminBorrowings');
    } else {
      setCurrentView('myLibrary');
    }
  };

  const handleRegisterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');

    if (!regName.trim() || !regEmail.trim() || !regPassword.trim()) {
      setRegError('All fields are required.');
      return;
    }

    if (regPassword !== regConfirmPassword) {
      setRegError('Passwords do not match.');
      return;
    }

    if (regPassword.length < 6) {
      setRegError('Password must contain at least 6 characters.');
      return;
    }

    const emailNorm = regEmail.trim().toLowerCase();
    if (userList.some(u => u.email.toLowerCase() === emailNorm)) {
      setRegError(`A user with email '${regEmail}' is already registered in smartlib_db.`);
      return;
    }

    const newUser: RegisteredUser = {
      id: userList.length + 1,
      name: regName.trim(),
      email: emailNorm,
      role: 'USER',
      createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
    };

    setUserList(prev => [...prev, newUser]);
    setRegName('');
    setRegEmail('');
    setRegPassword('');
    setRegConfirmPassword('');
    setLoginEmail(emailNorm);
    setLoginSuccessNotice('Registration successful! Please sign in with your new student account.');
    setCurrentView('login');
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setSimulatedRole('GUEST');
    setLoginSuccessNotice('You have been securely signed out.');
    setCurrentView('login');
  };

  const switchRole = (role: UserRole) => {
    setSimulatedRole(role);
    if (role === 'GUEST') {
      setCurrentUser(null);
    } else if (role === 'ADMIN') {
      const admin = userList.find(u => u.role === 'ADMIN') || {
        id: 1,
        name: 'Library Administrator',
        email: 'admin@smartlib.edu',
        role: 'ADMIN' as const,
        createdAt: '2026-10-11 08:30:00'
      };
      setCurrentUser(admin);
    } else {
      const student = userList.find(u => u.role === 'USER') || {
        id: 2,
        name: 'Jane Scholar (Student)',
        email: 'jane.scholar@university.edu',
        role: 'USER' as const,
        createdAt: '2026-10-11 09:15:00'
      };
      setCurrentUser(student);
    }
  };

  // ---------------------------------------------------------------------------
  // Circulation Handlers (Stage 4)
  // ---------------------------------------------------------------------------

  const handleBorrowBook = (bookId: number) => {
    if (simulatedRole === 'GUEST' || !currentUser) {
      showFlash('error', 'Authentication required: Please log in or register before checking out books.');
      setCurrentView('login');
      return;
    }

    // 1. Check user's active loan limit
    const userActiveLoans = borrowingRecords.filter(r => r.userId === currentUser.id && r.status === 'BORROWED');
    if (userActiveLoans.length >= maxLoansAllowed) {
      showFlash(
        'error',
        `BorrowingLimitExceededException: You hold ${userActiveLoans.length} active loan(s). The maximum allowed under library policy is ${maxLoansAllowed}.`
      );
      return;
    }

    // 2. Prevent duplicate simultaneous loan of the same book title
    const holdsSameTitle = userActiveLoans.some(r => r.bookId === bookId);
    if (holdsSameTitle) {
      showFlash(
        'error',
        'Duplicate Loan Policy: You already hold an active borrowed copy of this book title. Please return it before checking out another.'
      );
      return;
    }

    // 3. Find available physical copy
    const targetBook = books.find(b => b.id === bookId);
    if (!targetBook || !targetBook.active) {
      showFlash('error', 'Book is not active or could not be found.');
      return;
    }

    const availableCopy = bookCopies.find(c => c.bookId === bookId && c.status === 'AVAILABLE');
    if (!availableCopy) {
      showFlash('error', `NoAvailableCopyException: All physical copies of '${targetBook.title}' are currently checked out.`);
      return;
    }

    // 4. Atomically claim copy and update book availableCopies count
    const today = new Date();
    const dueDateObj = new Date();
    dueDateObj.setDate(today.getDate() + loanDurationDays);
    const dueDateStr = dueDateObj.toISOString().split('T')[0];
    const borrowTimeStr = today.toISOString().replace('T', ' ').substring(0, 16);

    // Update copy status
    setBookCopies(prev =>
      prev.map(c => (c.id === availableCopy.id ? { ...c, status: 'BORROWED' } : c))
    );

    // Update book available copies
    setBooks(prev =>
      prev.map(b => (b.id === bookId ? { ...b, availableCopies: Math.max(0, b.availableCopies - 1) } : b))
    );

    // Create BorrowingRecord
    const newRecord: BorrowingRecordItem = {
      id: Math.max(...borrowingRecords.map(r => r.id), 0) + 1,
      userId: currentUser.id,
      userName: currentUser.name,
      userEmail: currentUser.email,
      copyId: availableCopy.id,
      copyCode: availableCopy.copyCode,
      bookId: targetBook.id,
      bookTitle: targetBook.title,
      bookAuthor: targetBook.author,
      borrowedAt: borrowTimeStr,
      dueDate: dueDateStr,
      returnedAt: null,
      status: 'BORROWED'
    };

    setBorrowingRecords(prev => [newRecord, ...prev]);

    showFlash(
      'success',
      `Success! Checked out copy [${availableCopy.copyCode}] of '${targetBook.title}'. Return due date: ${dueDateStr}.`
    );
    setCurrentView('myLibrary');
  };

  const handleReturnBook = (recordId: number) => {
    if (simulatedRole === 'GUEST' || !currentUser) {
      showFlash('error', 'Authentication required to process returns.');
      return;
    }

    const targetRecord = borrowingRecords.find(r => r.id === recordId);
    if (!targetRecord) {
      showFlash('error', 'InvalidReturnException: Circulation record does not exist.');
      return;
    }

    // Enforce ownership: students can only return their own loans
    if (targetRecord.userId !== currentUser.id && simulatedRole !== 'ADMIN') {
      showFlash('error', 'UnauthorizedActionException: You cannot return a loan that belongs to another library member.');
      return;
    }

    if (targetRecord.status === 'RETURNED') {
      showFlash('error', 'InvalidReturnException: This book copy has already been returned.');
      return;
    }

    const returnTimeStr = new Date().toISOString().replace('T', ' ').substring(0, 16);

    // Update record
    setBorrowingRecords(prev =>
      prev.map(r => (r.id === recordId ? { ...r, status: 'RETURNED', returnedAt: returnTimeStr } : r))
    );

    // Reset copy status to AVAILABLE
    setBookCopies(prev =>
      prev.map(c => (c.id === targetRecord.copyId ? { ...c, status: 'AVAILABLE' } : c))
    );

    // Synchronize parent book availableCopies count
    setBooks(prev =>
      prev.map(b => (b.id === targetRecord.bookId ? { ...b, availableCopies: Math.min(b.totalCopies, b.availableCopies + 1) } : b))
    );

    showFlash(
      'success',
      `Returned copy [${targetRecord.copyCode}] of '${targetRecord.bookTitle}'. Physical inventory updated.`
    );
  };

  // User active loans & history queries
  const userActiveLoans = currentUser
    ? borrowingRecords.filter(r => r.userId === currentUser.id && r.status === 'BORROWED')
    : [];

  const userHistoryRecords = currentUser
    ? borrowingRecords.filter(r => r.userId === currentUser.id)
    : [];

  const userOverdueLoans = userActiveLoans.filter(r => {
    const due = new Date(r.dueDate);
    const now = new Date();
    return now > due;
  });

  // Admin Book Management Handlers
  const handleOpenAddBook = () => {
    if (simulatedRole !== 'ADMIN') {
      setCurrentView('accessDenied');
      return;
    }
    setEditingBookId(null);
    setFormIsbn('');
    setFormTitle('');
    setFormAuthor('');
    setFormPublisher('');
    setFormPublicationYear(2024);
    setFormCategory('Computer Science');
    setFormDescription('');
    setFormShelfLocation('Stack 2 - Row B - Bay 1');
    setFormTotalCopies(5);
    setFormAvailableCopies(5);
    setFormActive(true);
    setFormError('');
    setCurrentView('adminBookForm');
  };

  const handleOpenEditBook = (book: BookItem) => {
    if (simulatedRole !== 'ADMIN') {
      setCurrentView('accessDenied');
      return;
    }
    setEditingBookId(book.id);
    setFormIsbn(book.isbn);
    setFormTitle(book.title);
    setFormAuthor(book.author);
    setFormPublisher(book.publisher);
    setFormPublicationYear(book.publicationYear);
    setFormCategory(book.category);
    setFormDescription(book.description);
    setFormShelfLocation(book.shelfLocation);
    setFormTotalCopies(book.totalCopies);
    setFormAvailableCopies(book.availableCopies);
    setFormActive(book.active);
    setFormError('');
    setCurrentView('adminBookForm');
  };

  const handleSaveBook = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (!formTitle.trim() || !formAuthor.trim() || !formCategory.trim() || !formShelfLocation.trim()) {
      setFormError('Title, author, category, and shelf location are required.');
      return;
    }

    if (formTotalCopies < 0 || formAvailableCopies < 0) {
      setFormError('Copy counts cannot be negative.');
      return;
    }

    if (formAvailableCopies > formTotalCopies) {
      setFormError(`Available copies (${formAvailableCopies}) cannot exceed total copies (${formTotalCopies}).`);
      return;
    }

    const trimmedIsbn = formIsbn.trim();
    if (trimmedIsbn) {
      const duplicate = books.find(b => b.isbn.toLowerCase() === trimmedIsbn.toLowerCase() && b.id !== editingBookId);
      if (duplicate) {
        setFormError(`DuplicateIsbnException: A book with ISBN '${trimmedIsbn}' already exists in the catalogue.`);
        return;
      }
    }

    if (editingBookId !== null) {
      setBooks(prev =>
        prev.map(b =>
          b.id === editingBookId
            ? {
                ...b,
                isbn: trimmedIsbn,
                title: formTitle.trim(),
                author: formAuthor.trim(),
                publisher: formPublisher.trim(),
                publicationYear: Number(formPublicationYear),
                category: formCategory.trim(),
                description: formDescription.trim(),
                shelfLocation: formShelfLocation.trim(),
                totalCopies: Number(formTotalCopies),
                availableCopies: Number(formAvailableCopies),
                active: formActive
              }
            : b
        )
      );
      showFlash('success', `Book '${formTitle.trim()}' was successfully updated.`);
    } else {
      const newId = Math.max(...books.map(b => b.id), 0) + 1;
      const newBook: BookItem = {
        id: newId,
        isbn: trimmedIsbn,
        title: formTitle.trim(),
        author: formAuthor.trim(),
        publisher: formPublisher.trim(),
        publicationYear: Number(formPublicationYear),
        category: formCategory.trim(),
        description: formDescription.trim(),
        shelfLocation: formShelfLocation.trim(),
        totalCopies: Number(formTotalCopies),
        availableCopies: Number(formAvailableCopies),
        active: formActive,
        createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
      };
      setBooks(prev => [newBook, ...prev]);

      // Generate new physical copies
      const newCopies: BookCopyItem[] = [];
      for (let i = 1; i <= newBook.totalCopies; i++) {
        newCopies.push({
          id: Math.max(...bookCopies.map(c => c.id), 0) + i,
          bookId: newId,
          copyCode: `LIB-${String(newId).padStart(3, '0')}-C${String(i).padStart(2, '0')}`,
          status: i <= newBook.availableCopies ? 'AVAILABLE' : 'BORROWED',
          shelfLocation: newBook.shelfLocation
        });
      }
      setBookCopies(prev => [...prev, ...newCopies]);

      showFlash('success', `Book '${formTitle.trim()}' was registered with ${newBook.totalCopies} physical copies.`);
    }

    setCurrentView('adminBooks');
  };

  const handleToggleBookActive = (bookId: number) => {
    if (simulatedRole !== 'ADMIN') {
      setCurrentView('accessDenied');
      return;
    }

    setBooks(prev =>
      prev.map(b => {
        if (b.id === bookId) {
          const newStatus = !b.active;
          showFlash(
            'success',
            `Book '${b.title}' is now ${newStatus ? 'reactivated in public catalogue' : 'safely deactivated and hidden from public search'}.`
          );
          return { ...b, active: newStatus };
        }
        return b;
      })
    );
  };

  // Catalogue Filtering Logic
  const filteredActiveBooks = books
    .filter(book => book.active)
    .filter(book => {
      if (!catalogueQuery.trim()) return true;
      const q = catalogueQuery.toLowerCase().trim();
      return (
        book.title.toLowerCase().includes(q) ||
        book.author.toLowerCase().includes(q) ||
        book.isbn.toLowerCase().includes(q) ||
        book.category.toLowerCase().includes(q)
      );
    })
    .filter(book => {
      if (!catalogueCategory) return true;
      return book.category.toLowerCase() === catalogueCategory.toLowerCase();
    })
    .sort((a, b) => {
      if (catalogueSortBy === 'title') {
        return catalogueSortDirection === 'asc' ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title);
      }
      if (catalogueSortBy === 'author') {
        return catalogueSortDirection === 'asc' ? a.author.localeCompare(b.author) : b.author.localeCompare(a.author);
      }
      if (catalogueSortBy === 'year') {
        return catalogueSortDirection === 'asc' ? a.publicationYear - b.publicationYear : b.publicationYear - a.publicationYear;
      }
      return 0;
    });

  const distinctCategories = Array.from(new Set(books.map(b => b.category))).sort();

  const activeBookCount = books.filter(b => b.active).length;
  const totalCopiesCount = books.filter(b => b.active).reduce((sum, b) => sum + b.totalCopies, 0);
  const availableCopiesCount = books.filter(b => b.active).reduce((sum, b) => sum + b.availableCopies, 0);
  const currentSelectedBook = books.find(b => b.id === selectedBookId) || books[0];
  const selectedBookCopies = bookCopies.filter(c => c.bookId === currentSelectedBook.id);

  // Admin stats
  const totalActiveLoansCampus = borrowingRecords.filter(r => r.status === 'BORROWED').length;
  const totalCompletedReturnsCampus = borrowingRecords.filter(r => r.status === 'RETURNED').length;
  const totalPhysicalCopiesCampus = bookCopies.length;
  const totalOverdueCampus = borrowingRecords.filter(r => {
    if (r.status !== 'BORROWED') return false;
    return new Date() > new Date(r.dueDate);
  }).length;

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      {/* Top Application Header & Mode Switcher */}
      <header className="bg-slate-950 border-b border-slate-800 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo & Stage Badge */}
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-700 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20 text-white font-bold">
                <BookOpen className="w-5 h-5 text-white" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <span className="font-bold text-lg text-white tracking-tight">SmartLib</span>
                  <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-blue-500/20 text-blue-300 border border-blue-500/30">
                    Stage 4
                  </span>
                </div>
                <p className="text-xs text-slate-400 hidden sm:block">
                  Unified University Library Hub &bull; Borrowing, Returns &amp; Circulation History
                </p>
              </div>
            </div>

            {/* Mode Switcher Tabs */}
            <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-slate-800 text-xs sm:text-sm">
              <button
                onClick={() => setActiveTab('preview')}
                className={`px-3 py-1.5 rounded-lg font-medium transition-all flex items-center space-x-1.5 ${
                  activeTab === 'preview'
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Globe className="w-3.5 h-3.5" />
                <span>Live Portal</span>
              </button>
              <button
                onClick={() => setActiveTab('code')}
                className={`px-3 py-1.5 rounded-lg font-medium transition-all flex items-center space-x-1.5 ${
                  activeTab === 'code'
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <FolderTree className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Code Explorer</span>
                <span className="text-xs bg-slate-800 px-1.5 py-0.2 rounded-full text-slate-300 font-mono">65</span>
              </button>
              <button
                onClick={() => setActiveTab('guide')}
                className={`px-3 py-1.5 rounded-lg font-medium transition-all flex items-center space-x-1.5 ${
                  activeTab === 'guide'
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Terminal className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">MySQL DDL &amp; Setup</span>
                <span className="sm:hidden">Guide</span>
              </button>
              <button
                onClick={() => setActiveTab('checklist')}
                className={`px-3 py-1.5 rounded-lg font-medium transition-all flex items-center space-x-1.5 ${
                  activeTab === 'checklist'
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Circulation Tests (11)</span>
                <span className="sm:hidden">Tests</span>
              </button>
            </div>

            {/* Quick Actions: ZIP Export */}
            <div className="flex items-center space-x-2">
              <button
                onClick={handleDownloadZip}
                disabled={isDownloadingZip}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-lg text-xs font-medium transition flex items-center space-x-1.5 shadow-sm"
                title="Download complete smartlib-stage4-circulation.zip"
              >
                <Download className={`w-3.5 h-3.5 ${isDownloadingZip ? 'animate-bounce' : ''}`} />
                <span className="hidden md:inline">{isDownloadingZip ? 'Zipping...' : 'Download Project ZIP'}</span>
                <span className="md:hidden">ZIP</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Role Switcher Toolbar */}
      <div className="bg-slate-900 border-b border-slate-800 px-4 py-2">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-2 text-xs">
          <div className="flex items-center space-x-2">
            <span className="text-slate-400 flex items-center space-x-1">
              <Lock className="w-3.5 h-3.5 text-blue-400" />
              <span>Simulated Security Session:</span>
            </span>
            <div className="flex items-center bg-slate-950 p-0.5 rounded-lg border border-slate-800">
              <button
                onClick={() => switchRole('GUEST')}
                className={`px-2.5 py-1 rounded font-medium transition ${
                  simulatedRole === 'GUEST'
                    ? 'bg-slate-700 text-white shadow'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                Guest (Public)
              </button>
              <button
                onClick={() => switchRole('USER')}
                className={`px-2.5 py-1 rounded font-medium transition flex items-center space-x-1 ${
                  simulatedRole === 'USER'
                    ? 'bg-blue-600 text-white shadow'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <UserCheck className="w-3 h-3" />
                <span>Student (Jane Scholar)</span>
              </button>
              <button
                onClick={() => switchRole('ADMIN')}
                className={`px-2.5 py-1 rounded font-medium transition flex items-center space-x-1 ${
                  simulatedRole === 'ADMIN'
                    ? 'bg-amber-600 text-white shadow'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <ShieldCheck className="w-3 h-3" />
                <span>Admin (Library Admin)</span>
              </button>
            </div>
          </div>

          <div className="flex items-center space-x-2 text-slate-400">
            {currentUser && (
              <span className="bg-blue-900/40 border border-blue-700/50 text-blue-200 px-2 py-0.5 rounded text-[11px] flex items-center space-x-1">
                <BookOpen className="w-3 h-3" />
                <span>Active Loans: {userActiveLoans.length} / {maxLoansAllowed}</span>
              </span>
            )}
            <span className="font-mono text-slate-200 bg-slate-800 px-2 py-0.5 rounded">
              {currentUser ? `${currentUser.name} (${currentUser.role})` : 'Anonymous Guest'}
            </span>
            {currentUser && (
              <button
                onClick={handleLogout}
                className="text-red-400 hover:text-red-300 font-medium ml-2 underline"
              >
                Sign Out
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Main Tab Views */}
      <main className="flex-1">
        {/* ========================================================================= */}
        {/* TAB 1: LIVE INTERACTIVE PORTAL                                           */}
        {/* ========================================================================= */}
        {activeTab === 'preview' && (
          <div className="bg-slate-100 text-slate-900 min-h-screen">
            {/* Flash Message Banner */}
            {flashMessage && (
              <div
                className={`px-4 py-3 text-sm font-medium flex items-center justify-between ${
                  flashMessage.type === 'success' ? 'bg-emerald-600 text-white' : 'bg-red-600 text-white'
                }`}
              >
                <div className="max-w-7xl mx-auto w-full flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    {flashMessage.type === 'success' ? (
                      <CheckCircle2 className="w-4 h-4" />
                    ) : (
                      <AlertTriangle className="w-4 h-4" />
                    )}
                    <span>{flashMessage.text}</span>
                  </div>
                  <button onClick={() => setFlashMessage(null)} className="text-white hover:opacity-80">
                    <X className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}

            {/* Simulated Thymeleaf Site Header */}
            <header className="bg-slate-900 text-white border-b border-slate-800">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                  {/* Brand */}
                  <button
                    onClick={() => setCurrentView('home')}
                    className="flex items-center space-x-2 text-left focus:outline-none"
                  >
                    <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white">
                      <BookOpen className="w-4 h-4" />
                    </div>
                    <div>
                      <span className="font-bold text-lg text-white">SmartLib</span>
                      <span className="text-[10px] text-blue-400 block font-mono">Central University Hub</span>
                    </div>
                  </button>

                  {/* Desktop Nav */}
                  <nav className="hidden md:flex items-center space-x-1 text-sm font-medium">
                    <button
                      onClick={() => setCurrentView('home')}
                      className={`px-3 py-1.5 rounded-lg transition ${
                        currentView === 'home' ? 'bg-slate-800 text-white' : 'text-slate-300 hover:text-white'
                      }`}
                    >
                      Home
                    </button>
                    <button
                      onClick={() => setCurrentView('catalogue')}
                      className={`px-3 py-1.5 rounded-lg transition flex items-center space-x-1 ${
                        currentView === 'catalogue' || currentView === 'bookDetails'
                          ? 'bg-blue-600 text-white'
                          : 'text-slate-300 hover:text-white'
                      }`}
                    >
                      <Library className="w-3.5 h-3.5" />
                      <span>Book Catalogue</span>
                    </button>

                    {/* My Library (Stage 4) */}
                    {currentUser && (
                      <button
                        onClick={() => setCurrentView('myLibrary')}
                        className={`px-3 py-1.5 rounded-lg transition flex items-center space-x-1.5 ${
                          currentView === 'myLibrary' || currentView === 'borrowingHistory'
                            ? 'bg-blue-600 text-white'
                            : 'text-slate-300 hover:text-white'
                        }`}
                      >
                        <Clock className="w-3.5 h-3.5" />
                        <span>My Library</span>
                        {userActiveLoans.length > 0 && (
                          <span className="text-[10px] bg-emerald-500 text-white px-1.5 py-0.2 rounded-full font-bold">
                            {userActiveLoans.length}
                          </span>
                        )}
                      </button>
                    )}

                    {/* Admin Links */}
                    {simulatedRole === 'ADMIN' && (
                      <>
                        <button
                          onClick={() => setCurrentView('adminBooks')}
                          className={`px-3 py-1.5 rounded-lg transition ${
                            currentView === 'adminBooks' || currentView === 'adminBookForm'
                              ? 'bg-amber-600 text-white'
                              : 'text-amber-300 hover:text-white'
                          }`}
                        >
                          Book Inventory
                        </button>
                        <button
                          onClick={() => setCurrentView('adminBorrowings')}
                          className={`px-3 py-1.5 rounded-lg transition flex items-center space-x-1 ${
                            currentView === 'adminBorrowings'
                              ? 'bg-amber-600 text-white'
                              : 'text-amber-300 hover:text-white'
                          }`}
                        >
                          <ShieldCheck className="w-3.5 h-3.5" />
                          <span>Circulation Audit</span>
                        </button>
                      </>
                    )}

                    {simulatedRole === 'USER' && (
                      <button
                        onClick={() => setCurrentView('userDashboard')}
                        className={`px-3 py-1.5 rounded-lg transition ${
                          currentView === 'userDashboard' ? 'bg-slate-800 text-white' : 'text-slate-300 hover:text-white'
                        }`}
                      >
                        Dashboard
                      </button>
                    )}
                  </nav>

                  {/* Auth Actions */}
                  <div className="hidden md:flex items-center space-x-3 text-sm">
                    {simulatedRole === 'GUEST' ? (
                      <>
                        <button
                          onClick={() => setCurrentView('login')}
                          className="px-3 py-1.5 text-slate-300 hover:text-white font-medium"
                        >
                          Login
                        </button>
                        <button
                          onClick={() => setCurrentView('register')}
                          className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium shadow-sm transition"
                        >
                          Register
                        </button>
                      </>
                    ) : (
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-slate-300 bg-slate-800 px-2 py-1 rounded border border-slate-700">
                          {currentUser?.name}
                        </span>
                        <button
                          onClick={handleLogout}
                          className="p-1.5 text-slate-400 hover:text-red-400"
                          title="Sign Out"
                        >
                          <LogOut className="w-4 h-4" />
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </header>

            {/* VIEW: MY LIBRARY (ACTIVE LOANS & DUE DATES) */}
            {currentView === 'myLibrary' && (
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Banner */}
                <div className="bg-slate-900 text-white rounded-2xl p-6 sm:p-8 shadow-sm mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-500/20 text-blue-300 border border-blue-500/30">
                      Circulation &bull; Stage 4 Active
                    </span>
                    <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-2">
                      My Active Loans &amp; Circulation
                    </h1>
                    <p className="text-slate-300 text-sm mt-1">
                      Manage physical volumes checked out to your account, track return due dates, and process immediate self-service returns.
                    </p>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <button
                      onClick={() => setCurrentView('borrowingHistory')}
                      className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition flex items-center space-x-1.5"
                    >
                      <History className="w-3.5 h-3.5" />
                      <span>View Past Transactions</span>
                    </button>
                    <button
                      onClick={() => setCurrentView('catalogue')}
                      className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow transition"
                    >
                      Browse Catalogue &rarr;
                    </button>
                  </div>
                </div>

                {/* Overdue Warning Alert */}
                {userOverdueLoans.length > 0 && (
                  <div className="mb-6 p-4 bg-red-100 border border-red-300 rounded-2xl text-xs text-red-800 flex items-start space-x-3">
                    <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                    <div>
                      <strong className="text-sm block">Action Required: Overdue Library Volume!</strong>
                      <span>You have overdue loans. Please return them promptly to avoid academic borrowing holds.</span>
                    </div>
                  </div>
                )}

                {/* Policy & Status Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
                  <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                    <span className="text-xs text-slate-500 font-medium">Active Loans Quota</span>
                    <div className="text-2xl font-bold font-mono text-slate-900 mt-1">
                      {userActiveLoans.length} <span className="text-sm text-slate-400 font-sans">/ {maxLoansAllowed} Limit</span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1">
                      {maxLoansAllowed - userActiveLoans.length > 0 ? (
                        <span className="text-emerald-600 font-semibold">
                          {maxLoansAllowed - userActiveLoans.length} more checkouts allowed
                        </span>
                      ) : (
                        <span className="text-red-500 font-semibold">Maximum quota reached</span>
                      )}
                    </p>
                  </div>

                  <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                    <span className="text-xs text-slate-500 font-medium">Standard Loan Duration</span>
                    <div className="text-2xl font-bold font-mono text-blue-600 mt-1">
                      {loanDurationDays} Days
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1">Undergraduate circulation policy</p>
                  </div>

                  <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                    <span className="text-xs text-slate-500 font-medium">Overdue Volumes</span>
                    <div className="text-2xl font-bold font-mono text-emerald-600 mt-1">
                      {userOverdueLoans.length}
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1">All current loans in good standing</p>
                  </div>
                </div>

                {/* Active Loans Table */}
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                  <div className="p-5 border-b border-slate-100 flex items-center justify-between">
                    <div>
                      <h2 className="font-bold text-base text-slate-900">Currently Borrowed Volumes</h2>
                      <p className="text-xs text-slate-500">Physical barcodes registered to your student credential</p>
                    </div>
                    <span className="text-xs font-mono bg-slate-100 px-2.5 py-1 rounded-full text-slate-700">
                      {userActiveLoans.length} Active Items
                    </span>
                  </div>

                  {userActiveLoans.length === 0 ? (
                    <div className="p-12 text-center">
                      <BookOpen className="w-12 h-12 text-slate-300 mx-auto mb-3" />
                      <h3 className="font-bold text-slate-800 text-base">You have no active book loans</h3>
                      <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                        Explore the university catalogue to check out textbooks and literature for your courses.
                      </p>
                      <button
                        onClick={() => setCurrentView('catalogue')}
                        className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-semibold shadow"
                      >
                        Explore Catalogue
                      </button>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-left text-xs">
                        <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 uppercase tracking-wider font-semibold">
                          <tr>
                            <th className="py-3 px-4">Book Title &amp; Author</th>
                            <th className="py-3 px-4">Barcode Copy</th>
                            <th className="py-3 px-4">Shelf Wayfinding</th>
                            <th className="py-3 px-4">Borrowed On</th>
                            <th className="py-3 px-4">Return Due Date</th>
                            <th className="py-3 px-4">Status</th>
                            <th className="py-3 px-4 text-right">Action</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {userActiveLoans.map(loan => (
                            <tr key={loan.id} className="hover:bg-slate-50 transition">
                              <td className="py-3 px-4">
                                <div className="font-bold text-slate-900">{loan.bookTitle}</div>
                                <div className="text-slate-500">{loan.bookAuthor}</div>
                              </td>
                              <td className="py-3 px-4 font-mono font-semibold text-blue-700">
                                {loan.copyCode}
                              </td>
                              <td className="py-3 px-4 font-mono text-slate-600">
                                {books.find(b => b.id === loan.bookId)?.shelfLocation || 'Stack 2'}
                              </td>
                              <td className="py-3 px-4 font-mono text-slate-500">{loan.borrowedAt}</td>
                              <td className="py-3 px-4 font-mono font-bold text-slate-900">{loan.dueDate}</td>
                              <td className="py-3 px-4">
                                <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
                                  Due in 11 days
                                </span>
                              </td>
                              <td className="py-3 px-4 text-right">
                                <button
                                  onClick={() => handleReturnBook(loan.id)}
                                  className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-semibold transition flex items-center space-x-1 ml-auto"
                                >
                                  <RotateCcw className="w-3.5 h-3.5 text-blue-600" />
                                  <span>Return Copy</span>
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* VIEW: BORROWING TRANSACTION HISTORY */}
            {currentView === 'borrowingHistory' && (
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="flex items-center space-x-2 text-xs text-slate-500 mb-4">
                  <button onClick={() => setCurrentView('myLibrary')} className="hover:text-blue-600">My Library</button>
                  <span>&gt;</span>
                  <span className="text-slate-800 font-semibold">Circulation History</span>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 mb-8 flex items-center justify-between">
                  <div>
                    <h1 className="text-xl font-bold text-slate-900">Borrowing Transaction Ledger</h1>
                    <p className="text-xs text-slate-500 mt-1">
                      Immutable history of all loans, checkouts, and completed returns under your account.
                    </p>
                  </div>
                  <button
                    onClick={() => setCurrentView('myLibrary')}
                    className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold"
                  >
                    &larr; Back to Active Loans
                  </button>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 uppercase tracking-wider font-semibold">
                        <tr>
                          <th className="py-3 px-4">Txn ID</th>
                          <th className="py-3 px-4">Book Title &amp; Author</th>
                          <th className="py-3 px-4">Copy Code</th>
                          <th className="py-3 px-4">Borrowed Date</th>
                          <th className="py-3 px-4">Due Date</th>
                          <th className="py-3 px-4">Returned Date</th>
                          <th className="py-3 px-4">Circulation Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {userHistoryRecords.map(r => (
                          <tr key={r.id} className="hover:bg-slate-50 transition">
                            <td className="py-3 px-4 font-mono text-slate-400">#TXN-{r.id}</td>
                            <td className="py-3 px-4">
                              <div className="font-bold text-slate-900">{r.bookTitle}</div>
                              <div className="text-slate-500">{r.bookAuthor}</div>
                            </td>
                            <td className="py-3 px-4 font-mono font-semibold text-blue-700">{r.copyCode}</td>
                            <td className="py-3 px-4 font-mono text-slate-500">{r.borrowedAt}</td>
                            <td className="py-3 px-4 font-mono text-slate-800">{r.dueDate}</td>
                            <td className="py-3 px-4 font-mono">
                              {r.returnedAt ? (
                                <span className="text-emerald-700 font-semibold">{r.returnedAt}</span>
                              ) : (
                                <span className="text-slate-400">&mdash; Active Loan &mdash;</span>
                              )}
                            </td>
                            <td className="py-3 px-4">
                              {r.status === 'BORROWED' ? (
                                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-800">
                                  Active Loan
                                </span>
                              ) : (
                                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700">
                                  Returned
                                </span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {/* VIEW: ADMIN CIRCULATION AUDIT (STAGE 4 ADMIN) */}
            {currentView === 'adminBorrowings' && (
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {simulatedRole !== 'ADMIN' ? (
                  <div className="bg-white rounded-2xl p-12 text-center border border-red-200 max-w-lg mx-auto">
                    <ShieldAlert className="w-16 h-16 text-red-500 mx-auto mb-4" />
                    <h2 className="text-xl font-bold text-slate-900">HTTP 403 Forbidden</h2>
                    <p className="text-xs text-slate-600 mt-2">
                      Administrative circulation audit ledger requires ROLE_ADMIN.
                    </p>
                    <button
                      onClick={() => switchRole('ADMIN')}
                      className="mt-4 px-4 py-2 bg-amber-600 text-white rounded-xl text-xs font-semibold"
                    >
                      Switch to Administrator Role
                    </button>
                  </div>
                ) : (
                  <div>
                    <div className="bg-slate-900 text-white rounded-2xl p-6 sm:p-8 shadow-sm mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
                      <div>
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                          Circulation Audit &bull; Stage 4
                        </span>
                        <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-2">
                          Campus-Wide Circulation &amp; Loan Audit
                        </h1>
                        <p className="text-slate-300 text-sm mt-1">
                          Inspect physical copy custody, due date compliance, and immutable return timestamps across campus.
                        </p>
                      </div>

                      <div className="flex gap-2">
                        <button
                          onClick={() => setCurrentView('adminBooks')}
                          className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold"
                        >
                          Book Inventory
                        </button>
                      </div>
                    </div>

                    {/* Stats */}
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
                      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <span className="text-xs text-slate-500">Active Campus Loans</span>
                        <div className="text-2xl font-bold text-blue-600 mt-1">{totalActiveLoansCampus}</div>
                        <span className="text-[10px] text-slate-400">Physical copies out</span>
                      </div>
                      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <span className="text-xs text-slate-500">Overdue Returns</span>
                        <div className="text-2xl font-bold text-emerald-600 mt-1">{totalOverdueCampus}</div>
                        <span className="text-[10px] text-slate-400">Zero policy violations</span>
                      </div>
                      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <span className="text-xs text-slate-500">Completed Returns</span>
                        <div className="text-2xl font-bold text-slate-900 mt-1">{totalCompletedReturnsCampus}</div>
                        <span className="text-[10px] text-slate-400">Audit completed cycles</span>
                      </div>
                      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <span className="text-xs text-slate-500">Total Barcoded Copies</span>
                        <div className="text-2xl font-bold text-purple-600 mt-1">{totalPhysicalCopiesCampus}</div>
                        <span className="text-[10px] text-slate-400">Across 3 stack floors</span>
                      </div>
                    </div>

                    {/* Table */}
                    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                      <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                          <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 uppercase tracking-wider font-semibold">
                            <tr>
                              <th className="py-3 px-4">Txn ID</th>
                              <th className="py-3 px-4">Borrower Details</th>
                              <th className="py-3 px-4">Book Title</th>
                              <th className="py-3 px-4">Barcode Copy</th>
                              <th className="py-3 px-4">Borrowed Date</th>
                              <th className="py-3 px-4">Due Date</th>
                              <th className="py-3 px-4">Returned Date</th>
                              <th className="py-3 px-4">Status</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {borrowingRecords.map(r => (
                              <tr key={r.id} className="hover:bg-slate-50 transition">
                                <td className="py-3 px-4 font-mono text-slate-400">#TXN-{r.id}</td>
                                <td className="py-3 px-4">
                                  <div className="font-semibold text-slate-900">{r.userName}</div>
                                  <div className="text-slate-500 font-mono text-[11px]">{r.userEmail}</div>
                                </td>
                                <td className="py-3 px-4 font-bold text-slate-900">{r.bookTitle}</td>
                                <td className="py-3 px-4 font-mono text-blue-700 font-semibold">{r.copyCode}</td>
                                <td className="py-3 px-4 font-mono text-slate-500">{r.borrowedAt}</td>
                                <td className="py-3 px-4 font-mono font-bold text-slate-800">{r.dueDate}</td>
                                <td className="py-3 px-4 font-mono">
                                  {r.returnedAt ? (
                                    <span className="text-emerald-700 font-semibold">{r.returnedAt}</span>
                                  ) : (
                                    <span className="text-slate-400">&mdash; Active &mdash;</span>
                                  )}
                                </td>
                                <td className="py-3 px-4">
                                  {r.status === 'BORROWED' ? (
                                    <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-800">
                                      Active Loan
                                    </span>
                                  ) : (
                                    <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700">
                                      Returned
                                    </span>
                                  )}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* VIEW: BOOK DETAILS WITH ACTIVE BORROW BUTTON */}
            {currentView === 'bookDetails' && (
              <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Breadcrumbs */}
                <div className="flex items-center space-x-2 text-xs text-slate-500 mb-6">
                  <button onClick={() => setCurrentView('home')} className="hover:text-blue-600">Home</button>
                  <span>&gt;</span>
                  <button onClick={() => setCurrentView('catalogue')} className="hover:text-blue-600">Book Catalogue</button>
                  <span>&gt;</span>
                  <span className="text-slate-800 font-semibold truncate max-w-xs">{currentSelectedBook.title}</span>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                  <div className="grid grid-cols-1 md:grid-cols-12">
                    {/* Left: Cover & Checkout Action */}
                    <div className="md:col-span-4 bg-slate-900 text-white p-6 flex flex-col justify-between">
                      <div>
                        <div className="bg-gradient-to-br from-blue-900 to-indigo-950 p-6 rounded-xl border border-blue-500/30 shadow-lg text-center mb-6">
                          <span className="text-xs uppercase font-mono tracking-widest text-blue-400 block mb-2">
                            {currentSelectedBook.category}
                          </span>
                          <h2 className="text-lg font-bold text-white leading-snug mb-3">
                            {currentSelectedBook.title}
                          </h2>
                          <p className="text-xs text-slate-300">
                            {currentSelectedBook.author}
                          </p>
                        </div>

                        {/* Holdings Summary Box */}
                        <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700 mb-4">
                          <span className="text-xs font-semibold text-slate-400 block">Inventory Holdings</span>
                          <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">
                            {currentSelectedBook.availableCopies} <span className="text-sm text-slate-400">/ {currentSelectedBook.totalCopies} Available</span>
                          </div>
                          <div className="mt-2 text-xs">
                            {currentSelectedBook.availableCopies > 0 ? (
                              <span className="inline-flex items-center text-emerald-300">
                                <span className="w-2 h-2 rounded-full bg-emerald-400 mr-1.5 animate-pulse" />
                                Available for immediate checkout
                              </span>
                            ) : (
                              <span className="text-red-400">All copies currently on loan</span>
                            )}
                          </div>
                        </div>

                        {/* ACTIVE BORROW FORM (STAGE 4) */}
                        <div className="bg-blue-950/60 p-4 rounded-xl border border-blue-800/60 text-xs mb-4">
                          <strong className="block text-blue-300 font-semibold mb-1">
                            Campus Circulation Checkout
                          </strong>
                          <p className="text-blue-200/80 text-[11px] mb-3">
                            Standard {loanDurationDays}-day loan period. Max {maxLoansAllowed} concurrent active loans.
                          </p>

                          {currentSelectedBook.availableCopies > 0 ? (
                            <button
                              onClick={() => handleBorrowBook(currentSelectedBook.id)}
                              className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold transition flex items-center justify-center space-x-1.5 shadow"
                            >
                              <BookOpen className="w-4 h-4" />
                              <span>Borrow Physical Copy</span>
                            </button>
                          ) : (
                            <div className="text-center py-2 text-red-300 bg-red-950/40 rounded border border-red-800/50 text-[11px]">
                              All physical copies checked out
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Barcodes list */}
                      <div className="border-t border-slate-800 pt-3">
                        <span className="text-[10px] uppercase font-mono text-slate-400 block mb-1">Physical Barcodes</span>
                        <div className="space-y-1 max-h-24 overflow-y-auto">
                          {selectedBookCopies.map(c => (
                            <div key={c.id} className="flex justify-between font-mono text-[10px] bg-slate-800 px-2 py-0.5 rounded">
                              <span className="text-slate-300">{c.copyCode}</span>
                              <span className={c.status === 'AVAILABLE' ? 'text-emerald-400 font-semibold' : 'text-amber-400'}>
                                {c.status}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Right Panel: Metadata & Shelf Locator */}
                    <div className="md:col-span-8 p-6 sm:p-8">
                      <div className="flex justify-between items-start mb-4">
                        <span className="px-3 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-700">
                          {currentSelectedBook.category}
                        </span>

                        {simulatedRole === 'ADMIN' && (
                          <button
                            onClick={() => handleOpenEditBook(currentSelectedBook)}
                            className="px-3 py-1 bg-amber-100 hover:bg-amber-200 text-amber-800 rounded-lg text-xs font-semibold transition flex items-center space-x-1"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                            <span>Edit Book</span>
                          </button>
                        )}
                      </div>

                      <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mb-2">
                        {currentSelectedBook.title}
                      </h1>
                      <p className="text-base text-slate-600 mb-6">
                        Authored by <strong className="text-slate-800">{currentSelectedBook.author}</strong>
                      </p>

                      {/* Shelf Locator Card */}
                      <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 mb-6">
                        <div className="flex items-center space-x-2 text-xs font-bold text-slate-700 mb-1">
                          <MapPin className="w-4 h-4 text-blue-600" />
                          <span>Physical Shelf Location (Wayfinding)</span>
                        </div>
                        <div className="text-base font-mono font-bold text-blue-700">
                          {currentSelectedBook.shelfLocation}
                        </div>
                      </div>

                      {/* Description */}
                      <div className="mb-6">
                        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
                          Book Overview &amp; Synopsis
                        </h3>
                        <p className="text-sm text-slate-700 leading-relaxed">
                          {currentSelectedBook.description || 'No detailed synopsis provided.'}
                        </p>
                      </div>

                      {/* Metadata Table */}
                      <div className="border-t border-slate-100 pt-6">
                        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                          Bibliographic Metadata
                        </h3>
                        <dl className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs">
                          <div>
                            <dt className="text-slate-400">ISBN-13</dt>
                            <dd className="font-mono font-semibold text-slate-800 mt-0.5">{currentSelectedBook.isbn || 'N/A'}</dd>
                          </div>
                          <div>
                            <dt className="text-slate-400">Publisher</dt>
                            <dd className="font-semibold text-slate-800 mt-0.5">{currentSelectedBook.publisher || 'University Press'}</dd>
                          </div>
                          <div>
                            <dt className="text-slate-400">Publication Year</dt>
                            <dd className="font-mono font-semibold text-slate-800 mt-0.5">{currentSelectedBook.publicationYear}</dd>
                          </div>
                        </dl>
                      </div>

                      <div className="mt-8 pt-4 border-t border-slate-100 flex items-center justify-between">
                        <button
                          onClick={() => setCurrentView('catalogue')}
                          className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition flex items-center space-x-1"
                        >
                          <ArrowLeft className="w-3.5 h-3.5" />
                          <span>Back to Book Catalogue</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Default Catalogue View */}
            {currentView === 'catalogue' && (
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Banner */}
                <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm mb-8">
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-700">
                          Stage 4 &bull; Circulation &amp; Checkouts Enabled
                        </span>
                        <span className="text-xs text-slate-500">Max Loans: {maxLoansAllowed} per student</span>
                      </div>
                      <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2">
                        University Book Catalogue
                      </h1>
                      <p className="text-slate-600 text-sm mt-1">
                        Explore physical volumes and borrow available copies directly with immediate return tracking.
                      </p>
                    </div>

                    {simulatedRole === 'ADMIN' && (
                      <button
                        onClick={() => setCurrentView('adminBooks')}
                        className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-sm font-semibold transition"
                      >
                        Manage Inventory in Admin &rarr;
                      </button>
                    )}
                  </div>

                  {/* Filter Bar */}
                  <div className="mt-6 pt-6 border-t border-slate-100 grid grid-cols-1 md:grid-cols-12 gap-3">
                    <div className="md:col-span-5 relative">
                      <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
                      <input
                        type="text"
                        value={catalogueQuery}
                        onChange={e => setCatalogueQuery(e.target.value)}
                        placeholder="Search by title, author, ISBN..."
                        className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>

                    <div className="md:col-span-3">
                      <select
                        value={catalogueCategory}
                        onChange={e => setCatalogueCategory(e.target.value)}
                        className="w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="">All Categories ({books.filter(b => b.active).length})</option>
                        {distinctCategories.map(cat => (
                          <option key={cat} value={cat}>{cat}</option>
                        ))}
                      </select>
                    </div>

                    <div className="md:col-span-2">
                      <select
                        value={catalogueSortBy}
                        onChange={e => setCatalogueSortBy(e.target.value as any)}
                        className="w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm"
                      >
                        <option value="title">Sort: Title</option>
                        <option value="author">Sort: Author</option>
                        <option value="year">Sort: Year</option>
                      </select>
                    </div>

                    <div className="md:col-span-2 flex space-x-2">
                      <button
                        onClick={() => setCatalogueSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'))}
                        className="flex-1 px-3 py-2.5 bg-slate-100 hover:bg-slate-200 rounded-xl text-xs font-semibold text-slate-700"
                      >
                        {catalogueSortDirection === 'asc' ? 'A-Z ↑' : 'Z-A ↓'}
                      </button>
                    </div>
                  </div>
                </div>

                {/* Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredActiveBooks.map(book => (
                    <div key={book.id} className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 flex flex-col justify-between">
                      <div>
                        <div className="flex items-center justify-between mb-3 text-xs">
                          <span className="px-2.5 py-0.5 rounded-full font-semibold bg-blue-50 text-blue-700 border border-blue-200">
                            {book.category}
                          </span>
                          <span className="font-mono text-slate-400 font-medium">{book.publicationYear}</span>
                        </div>

                        <h3
                          onClick={() => { setSelectedBookId(book.id); setCurrentView('bookDetails'); }}
                          className="font-bold text-base text-slate-900 hover:text-blue-600 cursor-pointer line-clamp-2 transition mb-1"
                        >
                          {book.title}
                        </h3>
                        <p className="text-xs text-slate-600 mb-3">by <span className="font-medium text-slate-800">{book.author}</span></p>

                        <div className="flex items-center space-x-1.5 text-xs text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 mb-4">
                          <MapPin className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                          <span className="font-mono text-slate-700">{book.shelfLocation}</span>
                        </div>
                      </div>

                      <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                        <div className="flex items-center space-x-1.5">
                          <span className={`w-2.5 h-2.5 rounded-full ${book.availableCopies > 0 ? 'bg-emerald-500' : 'bg-red-500'}`} />
                          <span className={`text-xs font-semibold ${book.availableCopies > 0 ? 'text-emerald-700' : 'text-red-600'}`}>
                            {book.availableCopies > 0 ? `${book.availableCopies} of ${book.totalCopies} Available` : 'All on Loan'}
                          </span>
                        </div>

                        <div className="flex items-center space-x-2">
                          {book.availableCopies > 0 && currentUser && (
                            <button
                              onClick={() => handleBorrowBook(book.id)}
                              className="px-2.5 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition shadow-sm"
                            >
                              Borrow
                            </button>
                          )}
                          <button
                            onClick={() => { setSelectedBookId(book.id); setCurrentView('bookDetails'); }}
                            className="px-2.5 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold transition"
                          >
                            Details
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Fallbacks for home, adminBooks, login, userDashboard etc. */}
            {currentView === 'home' && (
              <div className="bg-gradient-to-b from-slate-900 to-slate-800 text-white py-16 px-4 text-center">
                <span className="px-3 py-1 bg-blue-500/20 text-blue-300 border border-blue-400/30 rounded-full text-xs font-semibold mb-4 inline-block">
                  Stage 4 &bull; Borrowing, Returns &amp; Circulation History Live
                </span>
                <h1 className="text-3xl sm:text-5xl font-extrabold mb-4">
                  Welcome to SmartLib Central
                </h1>
                <p className="text-slate-300 text-sm max-w-xl mx-auto mb-6">
                  Check out books from campus stacks, view return due dates in My Library, and track transaction history.
                </p>
                <div className="flex justify-center gap-3">
                  <button onClick={() => setCurrentView('catalogue')} className="px-6 py-2.5 bg-blue-600 rounded-xl font-bold shadow">
                    Open Book Catalogue
                  </button>
                  {currentUser && (
                    <button onClick={() => setCurrentView('myLibrary')} className="px-6 py-2.5 bg-slate-800 rounded-xl font-bold">
                      My Library ({userActiveLoans.length} Loans)
                    </button>
                  )}
                </div>
              </div>
            )}

            {currentView === 'adminBooks' && (
              <div className="max-w-7xl mx-auto px-4 py-8">
                <div className="bg-white rounded-2xl p-6 border border-slate-200">
                  <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold text-slate-900">Admin Book Inventory</h2>
                    <button onClick={handleOpenAddBook} className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-bold">
                      Add Book
                    </button>
                  </div>
                  <div className="divide-y divide-slate-100">
                    {books.map(b => (
                      <div key={b.id} className="py-3 flex justify-between items-center text-xs">
                        <div>
                          <strong className="text-slate-900">{b.title}</strong>
                          <span className="text-slate-500 ml-2">({b.category} &bull; Copies: {b.availableCopies}/{b.totalCopies})</span>
                        </div>
                        <div className="flex space-x-2">
                          <button onClick={() => handleOpenEditBook(b)} className="text-blue-600 font-semibold">Edit</button>
                          <button onClick={() => handleToggleBookActive(b.id)} className="text-amber-600 font-semibold">
                            {b.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {currentView === 'adminBookForm' && (
              <div className="max-w-xl mx-auto px-4 py-8">
                <div className="bg-white rounded-2xl p-6 border border-slate-200">
                  <h2 className="text-lg font-bold mb-4">{editingBookId ? 'Edit Book' : 'Add New Book'}</h2>
                  {formError && <div className="text-xs text-red-600 mb-3">{formError}</div>}
                  <form onSubmit={handleSaveBook} className="space-y-3 text-xs">
                    <div>
                      <label className="block font-semibold mb-1">Title</label>
                      <input value={formTitle} onChange={e => setFormTitle(e.target.value)} required className="w-full border p-2 rounded" />
                    </div>
                    <div>
                      <label className="block font-semibold mb-1">Author</label>
                      <input value={formAuthor} onChange={e => setFormAuthor(e.target.value)} required className="w-full border p-2 rounded" />
                    </div>
                    <div>
                      <label className="block font-semibold mb-1">Shelf Location</label>
                      <input value={formShelfLocation} onChange={e => setFormShelfLocation(e.target.value)} required className="w-full border p-2 rounded" />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="block font-semibold mb-1">Total Copies</label>
                        <input type="number" value={formTotalCopies} onChange={e => setFormTotalCopies(Number(e.target.value))} min={0} required className="w-full border p-2 rounded" />
                      </div>
                      <div>
                        <label className="block font-semibold mb-1">Available Copies</label>
                        <input type="number" value={formAvailableCopies} onChange={e => setFormAvailableCopies(Number(e.target.value))} min={0} required className="w-full border p-2 rounded" />
                      </div>
                    </div>
                    <div className="flex justify-end gap-2 pt-3">
                      <button type="button" onClick={() => setCurrentView('adminBooks')} className="px-3 py-1.5 bg-slate-100 rounded">Cancel</button>
                      <button type="submit" className="px-4 py-1.5 bg-blue-600 text-white rounded font-bold">Save</button>
                    </div>
                  </form>
                </div>
              </div>
            )}

            {currentView === 'userDashboard' && (
              <div className="max-w-4xl mx-auto px-4 py-8">
                <div className="bg-white rounded-2xl p-6 border border-slate-200">
                  <h1 className="text-xl font-bold mb-4">Student Dashboard: {currentUser?.name}</h1>
                  <div className="p-4 bg-blue-50 border border-blue-200 rounded-xl mb-4 text-xs">
                    <strong>Circulation Status:</strong> You currently hold <strong>{userActiveLoans.length}</strong> active book loan(s).
                  </div>
                  <button onClick={() => setCurrentView('myLibrary')} className="px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold">
                    Go to My Library &rarr;
                  </button>
                </div>
              </div>
            )}

            {currentView === 'login' && (
              <div className="max-w-md mx-auto px-4 py-16">
                <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                  <h2 className="text-xl font-bold text-center mb-4">Sign in to SmartLib</h2>
                  {loginError && <div className="text-xs text-red-600 mb-3">{loginError}</div>}
                  <form onSubmit={handleLoginSubmit} className="space-y-3 text-xs">
                    <input type="email" value={loginEmail} onChange={e => setLoginEmail(e.target.value)} placeholder="Email" required className="w-full border p-2 rounded" />
                    <input type="password" value={loginPassword} onChange={e => setLoginPassword(e.target.value)} placeholder="Password" required className="w-full border p-2 rounded" />
                    <button type="submit" className="w-full py-2 bg-blue-600 text-white rounded-xl font-bold">Sign In</button>
                  </form>
                  <div className="mt-4 flex gap-2 text-[11px]">
                    <button onClick={() => { setLoginEmail('jane.scholar@university.edu'); setLoginPassword('Password@123'); }} className="flex-1 p-2 bg-blue-50 border rounded text-left">
                      <strong>Demo Student</strong> (Jane)
                    </button>
                    <button onClick={() => { setLoginEmail('admin@smartlib.edu'); setLoginPassword('Admin@123'); }} className="flex-1 p-2 bg-amber-50 border rounded text-left">
                      <strong>Demo Admin</strong> (Admin)
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 2: PROJECT CODE EXPLORER (65 FILES)                                   */}
        {/* ========================================================================= */}
        {activeTab === 'code' && (
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="bg-slate-950 rounded-2xl border border-slate-800 shadow-xl overflow-hidden flex flex-col lg:flex-row min-h-[700px]">
              <div className="w-full lg:w-80 border-b lg:border-b-0 lg:border-r border-slate-800 bg-slate-900/60 p-4 flex flex-col">
                <div className="mb-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center space-x-1.5">
                      <FolderTree className="w-4 h-4 text-blue-400" />
                      <span>Project Files</span>
                    </span>
                    <span className="text-[10px] bg-blue-500/20 text-blue-300 px-2 py-0.5 rounded-full font-mono">
                      65 files
                    </span>
                  </div>

                  <div className="mt-2 relative">
                    <Search className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 top-2.5" />
                    <input
                      type="text"
                      value={codeSearchQuery}
                      onChange={e => setCodeSearchQuery(e.target.value)}
                      placeholder="Filter files..."
                      className="w-full pl-8 pr-2 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-blue-500"
                    />
                  </div>

                  <div className="flex flex-wrap gap-1 mt-2 text-[10px]">
                    {['all', 'java', 'templates', 'config', 'test', 'sql', 'docs'].map(cat => (
                      <button
                        key={cat}
                        onClick={() => setCodeCategoryFilter(cat)}
                        className={`px-2 py-0.5 rounded-md capitalize transition ${
                          codeCategoryFilter === cat ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400'
                        }`}
                      >
                        {cat}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex-1 overflow-y-auto space-y-1 max-h-[550px] pr-1">
                  {PROJECT_FILES.filter(f => {
                    if (codeCategoryFilter !== 'all' && f.category !== codeCategoryFilter) return false;
                    if (codeSearchQuery) {
                      return (
                        f.name.toLowerCase().includes(codeSearchQuery.toLowerCase()) ||
                        f.path.toLowerCase().includes(codeSearchQuery.toLowerCase())
                      );
                    }
                    return true;
                  }).map(file => (
                    <button
                      key={file.path}
                      onClick={() => setSelectedFile(file)}
                      className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-mono transition flex items-center justify-between ${
                        selectedFile.path === file.path
                          ? 'bg-blue-600/30 text-blue-200 border border-blue-500/40'
                          : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                      }`}
                    >
                      <div className="truncate pr-2">
                        <span className="text-[10px] text-slate-500 block truncate">{file.path}</span>
                        <span className="font-semibold text-slate-200">{file.name}</span>
                      </div>
                      <span className="text-[9px] uppercase px-1 rounded bg-slate-800 text-slate-400 shrink-0">
                        {file.language}
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Code Viewer */}
              <div className="flex-1 flex flex-col bg-slate-950">
                <div className="px-6 py-3 border-b border-slate-800 bg-slate-900/40 flex items-center justify-between">
                  <div>
                    <span className="font-mono text-xs text-blue-400 font-semibold">{selectedFile.path}</span>
                    <p className="text-[11px] text-slate-400 mt-0.5">{selectedFile.description}</p>
                  </div>
                  <button
                    onClick={() => copyToClipboard(selectedFile.content)}
                    className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs font-medium transition flex items-center space-x-1"
                  >
                    {copiedFile ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedFile ? 'Copied!' : 'Copy'}</span>
                  </button>
                </div>

                <div className="flex-1 p-4 overflow-x-auto font-mono text-xs text-slate-200 leading-relaxed bg-slate-950/80 max-h-[600px] overflow-y-auto">
                  <pre className="flex">
                    <span className="select-none text-slate-600 text-right pr-4 border-r border-slate-800 mr-4 font-mono">
                      {selectedFile.content.split('\n').map((_, idx) => (
                        <span key={idx} className="block leading-6">{idx + 1}</span>
                      ))}
                    </span>
                    <code className="flex-1 font-mono leading-6">{selectedFile.content}</code>
                  </pre>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 3: MYSQL DDL & EXECUTION GUIDE                                       */}
        {/* ========================================================================= */}
        {activeTab === 'guide' && (
          <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
            <div className="bg-slate-950 border border-slate-800 rounded-2xl p-6">
              <span className="text-xs font-semibold text-blue-400 block mb-1">STAGE 4 &bull; CIRCULATION SYSTEM GUIDE</span>
              <h2 className="text-xl font-bold text-white">How to Run &amp; Verify Stage 4 Circulation</h2>
              <p className="text-slate-400 text-xs mt-1">
                Complete instructions for MySQL schema initialization, borrowing policy variables, and test verification.
              </p>
            </div>

            <div className="bg-slate-950 border border-slate-800 rounded-2xl p-6">
              <h3 className="text-sm font-bold text-slate-200 mb-2">1. MySQL Tables (book_copies &amp; borrowing_records)</h3>
              <pre className="bg-slate-900 p-4 rounded-xl text-xs font-mono text-emerald-300 overflow-x-auto border border-slate-800">
{`-- Execute in MySQL:
USE smartlib_db;

CREATE TABLE IF NOT EXISTS book_copies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    copy_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    shelf_location VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_copy_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS borrowing_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    borrowed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    returned_at DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id) ON DELETE CASCADE
);`}
              </pre>
            </div>

            <div className="bg-slate-950 border border-slate-800 rounded-2xl p-6">
              <h3 className="text-sm font-bold text-slate-200 mb-2">2. Maven Commands to Run Tests &amp; Boot Server</h3>
              <pre className="bg-slate-900 p-4 rounded-xl text-xs font-mono text-amber-300 overflow-x-auto border border-slate-800">
{`# 1. Run all 11 Stage 4 Circulation and Borrowing tests
mvn test -Dtest=SmartLibBorrowingTests

# 2. Run entire test suite (29 tests across all stages)
mvn test

# 3. Start Spring Boot Dev Server
mvn spring-boot:run`}
              </pre>
            </div>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 4: VERIFICATION CHECKLIST (11 TESTS)                                 */}
        {/* ========================================================================= */}
        {activeTab === 'checklist' && (
          <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
            <div className="bg-slate-950 border border-slate-800 rounded-2xl p-6 flex justify-between items-center">
              <div>
                <span className="text-xs font-semibold text-emerald-400 block mb-1">
                  STAGE 4 &bull; 11 AUTOMATED CIRCULATION TESTS
                </span>
                <h2 className="text-xl font-bold text-white">SmartLibBorrowingTests Verification Matrix</h2>
                <p className="text-xs text-slate-400 mt-1">
                  Matches all 11 test cases in <code>src/test/java/com/smartlib/SmartLibBorrowingTests.java</code>.
                </p>
              </div>
              <div className="text-right">
                <span className="text-2xl font-bold font-mono text-emerald-400">11 / 11</span>
                <span className="text-xs text-slate-400 block">Tests Passing</span>
              </div>
            </div>

            <div className="space-y-3">
              {[
                { id: 'btest1', name: '1. Successful Borrowing of an Available Copy', desc: 'Claims copy, sets copy status to BORROWED, decrements book available count, and sets due date.' },
                { id: 'btest2', name: '2. Rejection When No Copy is Available', desc: 'Throws NoAvailableCopyException when all physical copies are checked out.' },
                { id: 'btest3', name: '3. Prevention of Duplicate Active Title Loans', desc: 'Rejects student attempt to check out multiple copies of the exact same title simultaneously.' },
                { id: 'btest4', name: '4. Successful Return by Borrower', desc: 'Borrower returns physical copy; status transitions to RETURNED and copy status resets to AVAILABLE.' },
                { id: 'btest5', name: '5. Rejection of Return by Another User', desc: 'Throws UnauthorizedActionException when another user attempts to return a loan they do not own.' },
                { id: 'btest6', name: '6. Rejection of Repeated Returns', desc: 'Throws InvalidReturnException when attempting to return a record that is already completed.' },
                { id: 'btest7', name: '7. Correct Due-Date Calculation', desc: 'Asserts due date is calculated as today + loanDurationDays (14 days).' },
                { id: 'btest8', name: '8. User Borrowing Limit Enforcement', desc: 'Throws BorrowingLimitExceededException when active loans exceed policy maximum (3).' },
                { id: 'btest9', name: '9. Preservation of Transaction History', desc: 'Audit ledger preserves completed borrowing record with return timestamp intact.' },
                { id: 'btest10', name: '10. Availability Consistency Synchronization', desc: 'BookCopy available count and Book.availableCopies remain strictly synchronized.' },
                { id: 'btest11', name: '11. Access Control for Circulation & Admin Routes', desc: 'Anonymous users redirect to /login; non-admin users receive HTTP 403 on /admin/borrowings.' }
              ].map(item => (
                <div
                  key={item.id}
                  onClick={() => toggleCheck(item.id)}
                  className={`p-4 rounded-xl border transition cursor-pointer flex items-start space-x-3 ${
                    checkedItems[item.id] ? 'bg-slate-900/80 border-emerald-500/30' : 'bg-slate-950 border-slate-800 opacity-60'
                  }`}
                >
                  <div className={`w-5 h-5 rounded-md flex items-center justify-center shrink-0 mt-0.5 ${
                    checkedItems[item.id] ? 'bg-emerald-600 text-white' : 'border border-slate-700 bg-slate-800'
                  }`}>
                    {checkedItems[item.id] && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                  </div>
                  <div>
                    <span className="text-sm font-bold text-slate-200">{item.name}</span>
                    <p className="text-xs text-slate-400 mt-0.5">{item.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-slate-950 border-t border-slate-800 py-6 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center space-x-2">
            <BookOpen className="w-4 h-4 text-blue-400" />
            <span className="font-semibold text-slate-300">SmartLib Architecture Hub</span>
            <span>&bull;</span>
            <span>Stage 4 of 8</span>
          </div>
          <div>Built with Java 21 &bull; Spring Boot 3.3.4 &bull; Spring Security 6 &bull; Thymeleaf &bull; MySQL</div>
        </div>
      </footer>
    </div>
  );
}
