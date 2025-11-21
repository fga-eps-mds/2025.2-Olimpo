import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/Home.module.css";

// ... Mantenha suas importações de imagem ...
import home from "./assets/home.png";
import home_hover from "./assets/home_hover.png";
import coracao from "./assets/coracao.png";
import coracao_hover from "./assets/coracao_hover.png";
import seta from "./assets/seta.png";
import seta_hover from "./assets/seta_hover.png";
import lupa from "./assets/lupa.png";
import lupa_hover from "./assets/lupa_hover.png";
import mais from "./assets/mais.png";
import mais_hover from "./assets/mais_hover.png";
import usuario from "./assets/usuario.png";

function PostCard({ data }) {
    return (
        <article className={styles.card}>
            <header className={styles.cardHeader}>
                <div className={styles.userBlock}>
                    <div className={styles.avatar}>
                        {data.avatarUrl ? (
                            <img
                                src={data.avatarUrl}
                                alt={data.userName}
                                style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }}
                            />
                        ) : (
                            <div style={{
                                width: '100%', height: '100%', borderRadius: '50%',
                                backgroundColor: '#f5f6fa', display: 'flex', alignItems: 'center', justifyContent: 'center',
                                fontSize: '16px', color: '#363940', fontWeight: 'bold'
                            }}>
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
                    <button className={styles.moreDots}>⋮</button>
                </div>
            </header>

            {data.mediaUrl && (
                <div className={styles.mediaBox}>
                    <img src={data.mediaUrl} alt="Mídia" style={{ width: '100%', height: '100%', objectFit: 'contain', borderRadius: '7px' }} />
                </div>
            )}

            <h3 className={styles.cardTitle}>{data.title}</h3>
            <p className={styles.cardText}>{data.description}</p>

            <div style={{ marginTop: "8px" }}>
                <span className={styles.invest}>Investimento: </span>
                <span>{data.investment}</span>
            </div>
        </article>
    );
}

export default function Home() {
    const [hovered, setHovered] = useState(false);
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchIdeas = async () => {
            const token = localStorage.getItem('token');

            // Se não tem token, nem tenta buscar, já manda pro login
            if (!token) {
                navigate('/');
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/api/ideas', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                // TRATAMENTO DE SESSÃO EXPIRADA (403)
                if (response.status === 403) {
                    console.warn("Sessão inválida. Redirecionando para login...");
                    localStorage.removeItem('token'); // Limpa o token inválido
                    navigate('/'); // Redireciona
                    return;
                }

                if (response.ok) {
                    const ideas = await response.json();

                    const mappedPosts = ideas.map(idea => ({
                        id: idea.id,
                        userName: idea.account.name,
                        avatarUrl: idea.account.pfp,
                        date: new Date(idea.time).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }),
                        segment: idea.keywords && idea.keywords.length > 0 ? idea.keywords[0].name : 'Geral',
                        title: idea.name,
                        description: idea.description,
                        investment: new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(idea.price),
                        mediaUrl: idea.ideaFiles && idea.ideaFiles.length > 0 ? idea.ideaFiles[0].fileUrl : null
                    }));

                    // Inverte para mostrar os mais novos primeiro
                    setPosts(mappedPosts.reverse());
                }
            } catch (err) {
                console.error('Erro de conexão:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchIdeas();
    }, [navigate]);

    return (
        <div className={styles.page}>
            <aside className={styles.sidebar} onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)}>
                <nav className={styles["menu-icons"]}>
                    <button onClick={() => navigate('/home')} className={styles["icon-btn"]}><img src={hovered ? home_hover : home} alt="home" /><span>Início</span></button>
                    <button className={styles["icon-btn"]}><img src={hovered ? coracao_hover : coracao} alt="not" /><span>Notificações</span></button>
                    <button className={styles["icon-btn"]}><img src={hovered ? seta_hover : seta} alt="msg" /><span>Mensagens</span></button>
                    <button className={styles["icon-btn"]}><img src={hovered ? lupa_hover : lupa} alt="search" /><span>Pesquisar</span></button>
                    <button onClick={() => navigate('/postar-ideia')} className={styles["icon-btn"]}><img src={hovered ? mais_hover : mais} alt="post" /><span>Postar</span></button>
                </nav>
                <div className={styles.profile}>
                    <button className={styles["profile-btn"]}><img src={usuario} alt="usu" /><span>Perfil</span></button>
                </div>
            </aside>

            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>
                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : posts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhuma publicação encontrada</h3>
                            <p>Seja o primeiro a compartilhar uma ideia!</p>
                        </div>
                    ) : (
                        <>
                            {posts.map((p) => <PostCard key={p.id} data={p} />)}
                            <div className={styles.endText}>Você viu todas as publicações recentes</div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}