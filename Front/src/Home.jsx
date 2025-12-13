import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/Home.module.css";
import { SEGMENTS } from './constants';
import Sidebar from "./components/Sidebar";

import lupa from './assets/lupa.png';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

const parseJwt = (token) => {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return null;
    }
};

function PostCard({ data, currentUserEmail, onDelete, onEdit, onLike, onProfileClick }) {
    const [menuOpen, setMenuOpen] = useState(false);
    const isOwner = data.userEmail === currentUserEmail;

    const handleProfileClick = () => {
        onProfileClick(data.userId);
    };

    return (
        <article className={styles.card}>
            <header className={styles.cardHeader}>
                <div
                    className={styles.userBlock}
                    onClick={handleProfileClick}
                    style={{ cursor: 'pointer' }}
                >
                    <div className={styles.avatar}>
                        {data.avatarUrl ? (
                            <img src={data.avatarUrl} alt={data.userName} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }} />
                        ) : (
                            <div style={{ width: '100%', height: '100%', borderRadius: '50%', backgroundColor: '#f5f6fa', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '16px', color: '#363940', fontWeight: 'bold' }}>
                                {data.userName ? data.userName.charAt(0).toUpperCase() : '?'}
                            </div>
                        )}
                    </div>
                    <div className={styles.userInfo}>
                        <span className={styles.name}>{data.userName}</span>
                        <span className={styles.date}>{data.date}</span>
                    </div>
                </div>

                <div className={styles.headerActions}>
                    <div className={styles.segmentBadge}>{data.segment}</div>

                    {isOwner && (
                        <div style={{ position: 'relative' }}>
                            <button
                                className={styles.moreDots}
                                onClick={() => setMenuOpen(!menuOpen)}
                            >
                                ⋮
                            </button>
                            {menuOpen && (
                                <div className={styles.dropdownMenuPost}>
                                    <div className={styles.dropdownItemPost} onClick={() => onEdit(data)}>Editar</div>
                                    <div className={styles.dropdownItemPost} onClick={() => onDelete(data.id)} style={{ color: 'red' }}>Excluir</div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </header>

            {data.mediaUrl && (
                <div className={styles.mediaBox}>
                    <img src={data.mediaUrl} alt="Mídia" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                </div>
            )}

            <h3 className={styles.cardTitle}>{data.title}</h3>
            <p className={styles.cardText}>{data.description}</p>

            <div style={{ marginTop: "8px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <span className={styles.invest}>Investimento: </span>
                    <span>{data.investment}</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
                    <button
                        onClick={() => onLike(data.id)}
                        className={styles.likeBtn}
                    >
                        <svg
                            viewBox="0 0 24 24"
                            className={`${styles.heart} ${data.isLiked ? styles.liked : ""}`}
                        >
                            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 
                            2 5.42 4.42 3 7.5 3c1.74 0 3.41 0.81 4.5 2.09C13.09 3.81 
                            14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 
                            11.54L12 21.35z"/>
                        </svg>
                    </button>
                    <span>{data.likeCount}</span>
                </div>

            </div>
        </article>
    );
}

export default function Home() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUserId, setCurrentUserId] = useState("");
    const [currentUserEmail, setCurrentUserEmail] = useState("");
    const navigate = useNavigate();

    const [searchTerm, setSearchTerm] = useState("");
    const [segmentoOpen, setSegmentoOpen] = useState(false);
    const [selectedSegmento, setSelectedSegmento] = useState("");
    const [investimentoOpen, setInvestimentoOpen] = useState(false);
    const [selectedInvestimento, setSelectedInvestimento] = useState("");
    const [sortBy, setSortBy] = useState("timestamp");
    const [sortOpen, setSortOpen] = useState(false);
    const dropdownRef = useRef(null);

    const handleDelete = async (ideaId) => {
        if (!window.confirm("Tem certeza que deseja excluir esta ideia?")) return;

        const token = localStorage.getItem('token');
        try {
            const response = await fetch(`http://localhost:8080/api/ideas/${ideaId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                setPosts(posts.filter(p => p.id !== ideaId));
                alert("Ideia excluída com sucesso!");
            } else {
                alert("Erro ao excluir ideia.");
            }
        } catch (error) {
            console.error("Erro ao deletar:", error);
        }
    };

    const handleEdit = (idea) => {
        navigate('/editar-ideia', { state: { idea } });
    };

    const handleLike = async (ideaId) => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert("Você precisa estar logado para curtir.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/ideas/${ideaId}/like`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const isLiked = await response.json();

                setPosts(posts.map(post => {
                    if (post.id === ideaId) {
                        return {
                            ...post,
                            isLiked: isLiked,
                            likeCount: isLiked ? post.likeCount + 1 : post.likeCount - 1
                        };
                    }
                    return post;
                }));
            } else {
                console.error("Erro ao curtir");
            }
        } catch (error) {
            console.error("Erro na requisição de like:", error);
        }
    };

    useEffect(() => {
        function handleClickOutside(event) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setSegmentoOpen(false);
                setInvestimentoOpen(false);
                setSortOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    useEffect(() => {
        const fetchIdeas = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/');
                return;
            }

            const userData = parseJwt(token);
            if (userData) {
                setCurrentUserId(userData.id || userData.sub);
                setCurrentUserEmail(userData.sub);
            }

            try {
                const response = await fetch('http://localhost:8080/api/ideas', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 403) {
                    localStorage.removeItem('token');
                    navigate('/');
                    return;
                }

                if (response.ok) {
                    const ideasData = await response.json();

                    const mappedPosts = ideasData.map(item => {
                        const idea = item.idea;
                        return {
                            id: idea.id,
                            userId: idea.account.id,
                            userEmail: idea.account.email,
                            userName: idea.account.name,
                            avatarUrl: idea.account.pfp,
                            date: new Date(idea.time).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }),
                            segment: idea.keywords && idea.keywords.length > 0 ? idea.keywords[0].name : 'Geral',
                            title: idea.name,
                            description: idea.description,
                            priceRaw: idea.price,
                            investment: new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(idea.price),
                            mediaUrl: idea.ideaFiles && idea.ideaFiles.length > 0 ? idea.ideaFiles[0].fileUrl : null,
                            likeCount: item.likeCount,
                            isLiked: item.isLiked
                        };
                    });

                    setPosts(mappedPosts.reverse());
                }
            } catch (err) {
                console.error('Erro:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchIdeas();
    }, [navigate]);

    const handleProfileClick = (userId) => {
        navigate(`/perfil/${userId}`);
    };

    const goToMyProfile = () => {
        navigate(`/perfil/${currentUserId}`);
    };

    const filteredPosts = posts.filter(post => {
        const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            post.description.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesSegment = selectedSegmento ? post.segment === selectedSegmento : true;

        let matchesInvestment = true;
        if (selectedInvestimento) {
            const price = post.priceRaw;
            if (selectedInvestimento === 'Até R$ 10.000') matchesInvestment = price <= 10000;
            else if (selectedInvestimento === 'R$ 10.000 - R$ 50.000') matchesInvestment = price > 10000 && price <= 50000;
            else if (selectedInvestimento === 'R$ 50.000 - R$ 100.000') matchesInvestment = price > 50000 && price <= 100000;
            else if (selectedInvestimento === 'Acima de R$ 100.000') matchesInvestment = price > 100000;
        }

        return matchesSearch && matchesSegment && matchesInvestment;
    }).sort((a, b) => {
        if (sortBy === 'likes') {
            return b.likeCount - a.likeCount;
        }
        // Default to timestamp (most recent first)
        // Assuming higher ID means more recent if date is not precise enough, 
        // or we can parse the date string if needed. 
        // But the initial fetch already reverses them, so let's rely on ID for stability or just keep array order if 'timestamp'.
        // Since we are filtering, the relative order is preserved. 
        // However, if we want to be explicit:
        return b.id - a.id;
    });

    return (
        <div className={styles.page}>
            <Sidebar onProfileClick={goToMyProfile} />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>

                    <div className={styles["search-section"]} ref={dropdownRef}>
                        <div className={styles["search-input-container"]}>
                            <input
                                className={styles["search-input"]}
                                type="text"
                                placeholder="Pesquisar ideias..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <img src={lupa} alt="Buscar" className={styles["search-icon"]} />
                        </div>

                        <div className={styles["filters-row"]}>
                            <div className={styles["filter-dropdown"]}>
                                <button
                                    className={`${styles["filter-btn"]} ${selectedSegmento ? styles.active : ''}`}
                                    onClick={() => { setSegmentoOpen(!segmentoOpen); setInvestimentoOpen(false); }}
                                >
                                    {selectedSegmento || "Segmento"}
                                    <img src={segmentoOpen ? setaCima : setaBaixo} alt="seta" />
                                </button>
                                {segmentoOpen && (
                                    <div className={styles["dropdown-menu"]}>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSelectedSegmento(""); setSegmentoOpen(false); }}>Todos</div>
                                        {SEGMENTS.map(opt => (
                                            <div key={opt} className={styles["dropdown-item"]} onClick={() => { setSelectedSegmento(opt); setSegmentoOpen(false); }}>{opt}</div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            <div className={styles["filter-dropdown"]}>
                                <button
                                    className={`${styles["filter-btn"]} ${selectedInvestimento ? styles.active : ''}`}
                                    onClick={() => { setInvestimentoOpen(!investimentoOpen); setSegmentoOpen(false); }}
                                >
                                    {selectedInvestimento || "Faixa de Preço"}
                                    <img src={investimentoOpen ? setaCima : setaBaixo} alt="seta" />
                                </button>
                                {investimentoOpen && (
                                    <div className={styles["dropdown-menu"]}>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSelectedInvestimento(""); setInvestimentoOpen(false); }}>Qualquer valor</div>
                                        {['Até R$ 10.000', 'R$ 10.000 - R$ 50.000', 'R$ 50.000 - R$ 100.000', 'Acima de R$ 100.000'].map(opt => (
                                            <div key={opt} className={styles["dropdown-item"]} onClick={() => { setSelectedInvestimento(opt); setInvestimentoOpen(false); }}>{opt}</div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {(selectedSegmento || selectedInvestimento || searchTerm) && (
                                <button
                                    className={styles["filter-btn"]}
                                    style={{ color: '#ff4b4b' }}
                                    onClick={() => { setSelectedSegmento(""); setSelectedInvestimento(""); setSearchTerm(""); }}
                                >
                                    Limpar ✕
                                </button>
                            )}

                            <div className={styles["filter-dropdown"]}>
                                <button
                                    className={`${styles["filter-btn"]} ${sortBy !== 'timestamp' ? styles.active : ''}`}
                                    onClick={() => { setSortOpen(!sortOpen); setSegmentoOpen(false); setInvestimentoOpen(false); }}
                                >
                                    {sortBy === 'timestamp' ? "Mais Recentes" : "Mais Curtidas"}
                                    <img src={sortOpen ? setaCima : setaBaixo} alt="seta" />
                                </button>
                                {sortOpen && (
                                    <div className={styles["dropdown-menu"]}>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSortBy("timestamp"); setSortOpen(false); }}>Mais Recentes</div>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSortBy("likes"); setSortOpen(false); }}>Mais Curtidas</div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : filteredPosts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhum resultado encontrado</h3>
                            <p>Tente ajustar seus filtros de pesquisa.</p>
                        </div>
                    ) : (
                        <>
                            {filteredPosts.map((p) => (
                                <PostCard
                                    key={p.id}
                                    data={p}
                                    currentUserEmail={currentUserEmail}
                                    currentUserId={currentUserId}
                                    onDelete={handleDelete}
                                    onEdit={handleEdit}
                                    onLike={handleLike}
                                    onProfileClick={handleProfileClick}
                                />
                            ))}
                            <div className={styles.endText}>Você viu todas as publicações</div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}