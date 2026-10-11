/**
 * SmartLib - University Library Hub JavaScript (Stage 1)
 * Minimal, accessible vanilla JavaScript for UI interactions
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile Navigation Menu Toggle
    const navToggle = document.getElementById('navToggle');
    const primaryNav = document.getElementById('primaryNav');

    if (navToggle && primaryNav) {
        navToggle.addEventListener('click', () => {
            const isExpanded = navToggle.getAttribute('aria-expanded') === 'true';
            navToggle.setAttribute('aria-expanded', String(!isExpanded));
            primaryNav.classList.toggle('active');
        });

        // Close mobile nav on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && primaryNav.classList.contains('active')) {
                primaryNav.classList.remove('active');
                navToggle.setAttribute('aria-expanded', 'false');
                navToggle.focus();
            }
        });
    }

    // 2. Stage 1 Placeholder Modal Handling
    const featureModal = document.getElementById('featureModal');
    const modalBackdrop = document.getElementById('modalBackdrop');
    const modalCloseBtn = document.getElementById('modalCloseBtn');
    const modalConfirmBtn = document.getElementById('modalConfirmBtn');
    const modalTitle = document.getElementById('modalTitle');
    const modalDescription = document.getElementById('modalDescription');
    const modalStageTag = document.getElementById('modalStageTag');

    const featureMetadata = {
        'catalogue': {
            title: 'Book Catalogue (Stage 3)',
            stage: 'Stage 3 Milestone',
            desc: 'The Book Catalogue search and inventory browser will be fully activated in Stage 3, connecting to the MySQL catalogue tables for title, author, and shelf lookup.'
        },
        'syllabus': {
            title: 'Syllabus Finder (Stage 4)',
            stage: 'Stage 4 Milestone',
            desc: 'Syllabus course-mapping and automated reading list association will be implemented in Stage 4.'
        },
        'shelf': {
            title: 'Find on Shelf (Stage 5)',
            stage: 'Stage 5 Milestone',
            desc: 'Physical stack mapping, library wing wayfinding, and floor plan locators will be built in Stage 5.'
        },
        'myLibrary': {
            title: 'My Library & Loans (Stage 6)',
            stage: 'Stage 6 Milestone',
            desc: 'Personal student accounts, borrowing history, renewal requests, and due-date deadline notifications will launch in Stage 6.'
        },
        'quest': {
            title: 'Library Quest & Rewards (Stage 7)',
            stage: 'Stage 7 Milestone',
            desc: 'Gamified learning quests, research literacy challenges, and digital achievement badges will launch in Stage 7.'
        },
        'login': {
            title: 'User Authentication (Stage 2)',
            stage: 'Stage 2 Milestone',
            desc: 'Spring Security authentication and user registration for students and faculty will be implemented in Stage 2.'
        }
    };

    function openModal(key) {
        if (!featureModal) return;
        const data = featureMetadata[key] || {
            title: 'Feature Planned',
            stage: 'Future Milestone',
            desc: 'This capability will be activated in upcoming development stages.'
        };
        if (modalTitle) modalTitle.textContent = data.title;
        if (modalDescription) modalDescription.textContent = data.desc;
        if (modalStageTag) modalStageTag.textContent = data.stage;

        featureModal.classList.add('active');
        featureModal.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
        if (!featureModal) return;
        featureModal.classList.remove('active');
        featureModal.setAttribute('aria-hidden', 'true');
    }

    if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
    if (modalBackdrop) modalBackdrop.addEventListener('click', closeModal);
    if (modalConfirmBtn) modalConfirmBtn.addEventListener('click', closeModal);

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && featureModal && featureModal.classList.contains('active')) {
            closeModal();
        }
    });

    // 3. Attach click listeners to feature cards and placeholder links
    document.querySelectorAll('.feature-card a.feature-link, .nav-auth a').forEach(link => {
        const href = link.getAttribute('href');
        if (href && href.startsWith('/')) {
            const keyMatch = Object.keys(featureMetadata).find(k => href.includes(k) || (k === 'catalogue' && href.includes('catalogue')) || (k === 'shelf' && href.includes('shelf')));
            if (keyMatch) {
                link.addEventListener('click', (e) => {
                    // Show helpful modal before navigating or for preview
                    if (!e.ctrlKey && !e.metaKey) {
                        e.preventDefault();
                        openModal(keyMatch);
                    }
                });
            }
        }
    });

    // 4. Update dynamic copyright year
    const yearSpan = document.getElementById('currentYear');
    if (yearSpan) {
        yearSpan.textContent = new Date().getFullYear();
    }
});
