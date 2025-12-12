import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import styles from './styles/Home.module.css';

import usuario from './assets/usuario.png';

function ProfilePage() {
    const { id } = useParams();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isOwner, setIsOwner] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        async function load() {
            setLoading(true);
            try {
                const token = localStorage.getItem('token');
                const headers = {};
                let currentUserId = null;

                if (token) {
                    headers['Authorization'] = `Bearer ${token}`;
                    try {
                        const payload = JSON.parse(atob(token.split('.')[1]));
                        currentUserId = payload.id;
                    } catch (e) {
                        console.error("Erro ao decodificar token", e);
                    }
                }

                const resp = await fetch(`http://localhost:8080/user/${id}`, { headers });
                if (!resp.ok) throw new Error('Perfil não encontrado');
                const data = await resp.json();
                setProfile(data);

                if (currentUserId && data.id === currentUserId) {
                    setIsOwner(true);
                }
            } catch (err) {
                console.error(err);
                setProfile(null);
            } finally {
                setLoading(false);
            }
        }
        load();
    }, [id]);

    return (
        <div className={styles.page}>
            <Sidebar />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>
                    {loading ? (
                        <div className={styles.loading}>Carregando perfil...</div>
                    ) : !profile ? (
                        <div className={styles.noPosts}>
                            <h3>Perfil não encontrado</h3>
                            <Link to="/perfil/search" style={{ color: '#ff4b4b', textDecoration: 'none' }}>Voltar à busca</Link>
                        </div>
                    ) : (
                        <div style={{ padding: '20px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                                <Link to="/perfil/search" style={{ color: '#666', textDecoration: 'none' }}>← Voltar</Link>
                                {isOwner && (
                                    <button
                                        onClick={() => navigate('/editar-perfil')}
                                        style={{
                                            padding: '8px 16px',
                                            backgroundColor: '#22212b',
                                            color: '#fff',
                                            border: 'none',
                                            borderRadius: '8px',
                                            cursor: 'pointer',
                                            fontWeight: '600'
                                        }}
                                    >
                                        Editar Perfil
                                    </button>
                                )}
                            </div>

                            <div className={styles.card}>
                                <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
                                    <img
                                        src={profile.pfp || usuario}
                                        alt="pfp"
                                        style={{ width: '120px', height: '120px', borderRadius: '50%', objectFit: 'cover' }}
                                    />
                                    <div style={{ flex: 1 }}>
                                        <h2 style={{ margin: '0 0 8px 0', fontSize: '24px', color: '#333' }}>{profile.name}</h2>
                                        <p style={{ color: '#666', margin: '0 0 16px 0', fontSize: '14px' }}>
                                            {profile.role}
                                            {profile.faculdade && ` • ${profile.faculdade}`}
                                            {profile.curso && ` • ${profile.curso}`}
                                        </p>

                                        <div style={{ marginBottom: '20px' }}>
                                            <h4 style={{ margin: '0 0 8px 0', color: '#444' }}>Biografia</h4>
                                            <p style={{ color: '#555', lineHeight: '1.5' }}>{profile.bio || 'Sem biografia pública.'}</p>
                                        </div>

                                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '16px', borderTop: '1px solid #eee', paddingTop: '16px' }}>
                                            <div>
                                                <strong style={{ display: 'block', color: '#888', fontSize: '12px', marginBottom: '4px' }}>ESTADO</strong>
                                                <span style={{ color: '#333' }}>{profile.estado || '-'}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default ProfilePage;
