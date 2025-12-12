import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/VisualizarPerfil.module.css";
import Sidebar from "./components/Sidebar";

import coracao from './assets/coracao.png';
import coracaoHover from './assets/coracao_hover.png';
import usuario from './assets/usuario.png'

const parseJwt = (token) => {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return null;
    }
};

function PostCard({ data, currentUserEmail, onDelete, onEdit, onLike }) {
    const [menuOpen, setMenuOpen] = useState(false);
    const isOwner = data.userEmail === currentUserEmail;

    return (
        <article className={styles.card}>
            <header className={styles.cardHeader}>
                <div className={styles.userBlock}>
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
                        style={{ background: "none", border: "none", cursor: "pointer", padding: 0, display: "flex", alignItems: "center" }}
                    >
                        <img
                            src={data.isLiked ? coracaoHover : coracao}
                            alt="Like"
                            style={{ width: "20px", height: "20px" }}
                        />
                    </button>
                    <span>{data.likeCount}</span>
                </div>
            </div>
        </article>
    );
}

export default function VisualizarPerfil() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUserEmail, setCurrentUserEmail] = useState("");
    const navigate = useNavigate();

    const [searchTerm, setSearchTerm] = useState("");
    const [segmentoOpen, setSegmentoOpen] = useState(false);
    const [selectedSegmento, setSelectedSegmento] = useState("");
    const [investimentoOpen, setInvestimentoOpen] = useState(false);
    const [selectedInvestimento, setSelectedInvestimento] = useState("");
    const dropdownRef = useRef(null);

    const [profileData, setProfileData] = useState({
        name: "Nome do usuário",
        fullName: "Nome completo",
        followers: 0,
        description: "Descrição",
        avatar: usuario
    });

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
            if (userData) setCurrentUserEmail(userData.sub);

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

                    const userIdeas = ideasData.filter(item => {
                        return item.idea.account.email === userData.sub;
                    });

                    const mappedPosts = userIdeas.map(item => {
                        const idea = item.idea;
                        return {
                            id: idea.id,
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
                    
                    if (userIdeas.length > 0) {
    const firstIdea = userIdeas[0].idea;
    console.log("Dados do usuário da primeira ideia:", {
        name: firstIdea.account.name,
        pfp: firstIdea.account.pfp,
        email: firstIdea.account.email
    });
    
    setProfileData({
        name: firstIdea.account.name || "Nome do usuário",
        fullName: firstIdea.account.name || "Nome completo",
        followers: 0,
        description: "Descrição",
        avatar: firstIdea.account.pfp || usuario
    });
}
                }
            } catch (err) {
                console.error('Erro:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchIdeas();
    }, [navigate]);

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
    });

    return (
        <div className={styles.page}>
            <Sidebar />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>
                    
                    <div className={styles.container}>
                        <div className={styles.imagem}>
                            {profileData.avatar && profileData.avatar !== usuario ? (
                                <img src={profileData.avatar} alt="Perfil" />
                            ) : (
                                <div className={styles.placeholder}>
                                    {profileData.name ? profileData.name.charAt(0).toUpperCase() : '?'}
                                </div>
                            )}
                        </div>
                        <div className={styles.container2}>
                            <div className={styles.nome}>{profileData.name}</div>
                            <div className={styles.texto}>{profileData.fullName}</div>
                            <div className={styles.texto}>{profileData.followers} seguidores</div>
                            <div className={styles.texto}>{profileData.description}</div>
                        </div>
                    </div>
                    <div className={styles.buttonContainer}>
                        <button className={styles.editar} type="submit">
                            Editar perfil
                        </button>
                    </div>
                    

                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : filteredPosts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhuma publicação encontrada</h3>
                            <p>Você ainda não fez nenhuma publicação.</p>
                        </div>
                    ) : (
                        <>
                            {filteredPosts.map((p) => (
                                <PostCard
                                    key={p.id}
                                    data={p}
                                    currentUserEmail={currentUserEmail}
                                    onDelete={handleDelete}
                                    onEdit={handleEdit}
                                    onLike={handleLike}
                                />
                            ))}
                            <div className={styles.endText}>Você viu todas as suas publicações.</div>
                        </>
                    )}
                </div>
                
            </main>
        </div>
    );
}