import React, { useState, useEffect, useRef } from "react";
import styles from "./styles/Home.module.css";

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
                width: '100%', 
                height: '100%', 
                borderRadius: '50%', 
                backgroundColor: '#f5f6fa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '16px',
                color: '#363940',
                fontWeight: 'bold'
              }}>
                {data.userName.charAt(0)}
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
          <img 
            src={data.mediaUrl} 
            alt="Mídia do post"
            style={{ 
              width: '100%', 
              height: '100%', 
              objectFit: 'cover',
              borderRadius: '7px'
            }}
          />
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
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const sentinelRef = useRef(null);

  // Função para carregar posts da API
  const loadPosts = async () => {
    if (loading || !hasMore) return;
    
    setLoading(true);

    try {
      const response = await fetch(`/api/posts?page=${page}&limit=5`);
      
      if (response.ok) {
        const data = await response.json();
        
        if (data.posts && data.posts.length > 0) {
          setPosts((prevPosts) => [...prevPosts, ...data.posts]);
          setHasMore(data.pagination.hasMore);
        } else {
          setHasMore(false);
        }
      }
      // Se houver erro, simplesmente não carrega mais posts
    } catch (err) {
      console.error('Erro ao carregar posts:', err);
      setHasMore(false);
    } finally {
      setLoading(false);
    }
  };

  // Carrega posts quando a página muda
  useEffect(() => {
    loadPosts();
  }, [page]);

  // IntersectionObserver para infinite scroll
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !loading && hasMore) {
          setPage((prev) => prev + 1);
        }
      },
      { root: null, rootMargin: "100px", threshold: 0.1 }
    );

    const el = sentinelRef.current;
    if (el && hasMore) observer.observe(el);

    return () => {
      if (el) observer.unobserve(el);
      observer.disconnect();
    };
  }, [loading, hasMore]);

  return (
    <div className={styles.page}>
      <aside
        className={styles.sidebar}
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        <nav className={styles["menu-icons"]}>
          <button className={styles["icon-btn"]}>
            <img src={hovered ? home_hover : home} alt="home" />
            <span>Início</span>
          </button>

          <button className={styles["icon-btn"]}>
            <img src={hovered ? coracao_hover : coracao} alt="not" />
            <span>Notificações</span>
          </button>

          <button className={styles["icon-btn"]}>
            <img src={hovered ? seta_hover : seta} alt="msg" />
            <span>Mensagens</span>
          </button>

          <button className={styles["icon-btn"]}>
            <img src={hovered ? lupa_hover : lupa} alt="search" />
            <span>Pesquisar</span>
          </button>

          <button className={styles["icon-btn"]}>
            <img src={hovered ? mais_hover : mais} alt="post" />
            <span>Postar</span>
          </button>
        </nav>

        <div className={styles.profile}>
          <button className={styles["profile-btn"]}>
            <img src={usuario} alt="usu" />
            <span>Perfil</span>
          </button>
        </div>
      </aside>

        <main className={styles["feed-container"]}>
            <div className={styles["feed-inner"]}>
          {posts.length === 0 && !loading && !hasMore ? (
            <div className={styles.noPosts}>
              <h3>Nenhuma publicação recente</h3>
              <p>Quando houver novas publicações, elas aparecerão aqui.</p>
            </div>
          ) : (
            <>
              {posts.map((p) => (
                <PostCard key={p.id} data={p} />
              ))}

              <div ref={sentinelRef} className={styles.sentinel}>
                {loading && <div className={styles.loading}>Carregando mais publicações...</div>}
              </div>

              {!hasMore && posts.length > 0 && (
                <div className={styles.endText}>Você viu todas as publicações recentes</div>
              )}
            </>
          )}
          </div>
        </main>
    </div>
  );
}