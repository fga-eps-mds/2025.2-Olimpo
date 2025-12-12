import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import styles from "./styles/VisualizarPerfil.module.css";
import Sidebar from "./components/Sidebar";


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

export default function VisualizarPerfil() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUserEmail, setCurrentUserEmail] = useState("");
    const navigate = useNavigate();



    const [profileData, setProfileData] = useState({
        name: "Nome do usuário",
        email: "email@exemplo.com",
        curso: "Curso",
        faculdade: "",
        role: "ESTUDANTE",
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



    const { id } = useParams();

    useEffect(() => {
        const fetchIdeasAndProfile = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/');
                return;
            }

            const userData = parseJwt(token);
            if (userData) setCurrentUserEmail(userData.sub);

            try {
                // Fetch Profile Data
                const profileResponse = await fetch(`http://localhost:8080/user/${id}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (profileResponse.ok) {
                    const profile = await profileResponse.json();
                    setProfileData({
                        name: profile.name || "Nome do usuário",
                        email: profile.email || "Email não disponível",
                        curso: profile.curso || "",
                        faculdade: profile.faculdade || "",
                        role: profile.role || "ESTUDANTE",
                        description: profile.bio || "Descrição",
                        avatar: profile.pfp || usuario
                    });
                }

                // Fetch Ideas
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
                }
            } catch (err) {
                console.error('Erro:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchIdeasAndProfile();
    }, [navigate, id]);



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
                            <div className={styles.nome}>
                                {profileData.name} - {profileData.role === 'INVESTIDOR' ? 'Investidor' : 'Estudante'}
                            </div>
                            <div className={styles.texto}>
                                {profileData.curso || "Curso não informado"}
                                {profileData.role === 'ESTUDANTE' && profileData.faculdade ? ` | ${profileData.faculdade}` : ''}
                            </div>
                            <div className={styles.texto}>{profileData.email}</div>
                            <div className={styles.texto}>{profileData.description}</div>
                        </div>
                    </div>
                    <div className={styles.buttonContainer}>
                        <button className={styles.editar} onClick={() => navigate('/editar-perfil')}>
                            Editar perfil
                        </button>
                    </div>


                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : posts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhuma publicação encontrada</h3>
                            <p>Você ainda não fez nenhuma publicação.</p>
                        </div>
                    ) : (
                        <>
                            {posts.map((p) => (
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