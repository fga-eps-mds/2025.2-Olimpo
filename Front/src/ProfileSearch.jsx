import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import styles from './styles/Home.module.css';
import lupa from './assets/lupa.png';

function ProfileSearch() {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [hasSearched, setHasSearched] = useState(false);
    const navigate = useNavigate();

    async function doSearch(e) {
        e && e.preventDefault();
        setLoading(true);
        setHasSearched(true);
        try {
            const token = localStorage.getItem('token');
            const headers = {};
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const resp = await fetch(`http://localhost:8080/user/search?name=${encodeURIComponent(query)}`, { headers });
            if (!resp.ok) throw new Error('Erro na busca');
            const data = await resp.json();
            setResults(data);
        } catch (err) {
            console.error(err);
            setResults([]);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className={styles.page}>
            <Sidebar />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>
                    <div className={styles["search-section"]}>
                        <form onSubmit={doSearch} style={{ width: '100%' }}>
                            <div className={styles["search-input-container"]}>
                                <input
                                    className={styles["search-input"]}
                                    type="text"
                                    placeholder="Buscar usuário por nome..."
                                    value={query}
                                    onChange={e => {
                                        setQuery(e.target.value);
                                        setHasSearched(false);
                                    }}
                                />
                                <button type="submit" style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                                    <img src={lupa} alt="Buscar" className={styles["search-icon"]} />
                                </button>
                            </div>
                        </form>
                    </div>

                    {loading ? (
                        <div className={styles.loading}>Buscando...</div>
                    ) : (
                        <div style={{ marginTop: '20px' }}>
                            {results.length === 0 && hasSearched && !loading && (
                                <div className={styles.noPosts}>
                                    <h3>Nenhum usuário encontrado</h3>
                                    <p>Tente buscar por outro nome.</p>
                                </div>
                            )}

                            {results.map(r => (
                                <div key={r.id} className={styles.card} style={{ marginBottom: '16px', cursor: 'pointer' }} onClick={() => navigate(`/perfil/${r.id}`)}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                                        <img
                                            src={r.pfp || '/public/avatar.png'}
                                            alt="pfp"
                                            style={{ width: '64px', height: '64px', borderRadius: '50%', objectFit: 'cover' }}
                                        />
                                        <div style={{ flex: 1 }}>
                                            <h3 style={{ margin: '0 0 4px 0', fontSize: '18px', color: '#333' }}>{r.name}</h3>
                                            <p style={{ margin: 0, color: '#666', fontSize: '14px' }}>
                                                {r.role}
                                                {(r.faculdade || r.curso) && ` • ${r.faculdade || ''} ${r.curso || ''}`}
                                            </p>
                                            {r.estado && <p style={{ margin: '4px 0 0 0', color: '#888', fontSize: '12px' }}>{r.estado}</p>}
                                        </div>
                                        <button
                                            style={{
                                                padding: '8px 16px',
                                                borderRadius: '20px',
                                                border: '1px solid #ddd',
                                                background: 'white',
                                                cursor: 'pointer',
                                                fontWeight: '600',
                                                color: '#555'
                                            }}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                navigate(`/perfil/${r.id}`);
                                            }}
                                        >
                                            Ver Perfil
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default ProfileSearch;
