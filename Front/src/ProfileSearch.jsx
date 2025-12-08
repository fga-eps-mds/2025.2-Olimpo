import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function ProfileSearch() {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    async function doSearch(e) {
        e && e.preventDefault();
        setLoading(true);
        try {
            const resp = await fetch(`http://localhost:8080/user/search?name=${encodeURIComponent(query)}`);
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
        <div style={{ padding: 20 }}>
            <h2>Buscar Perfis</h2>
            <form onSubmit={doSearch}>
                <input
                    placeholder="Nome do usuário"
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    style={{ padding: 8, width: 300 }}
                />
                <button type="submit" style={{ marginLeft: 8 }}>Pesquisar</button>
            </form>

            {loading && <p>Buscando...</p>}

            <ul style={{ listStyle: 'none', padding: 0 }}>
                {results.map(r => (
                    <li key={r.id} style={{ marginTop: 12, borderBottom: '1px solid #eee', paddingBottom: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                            <img src={r.pfp || '/public/avatar.png'} alt="pfp" style={{ width: 48, height: 48, borderRadius: '50%' }} />
                            <div style={{ flex: 1 }}>
                                <div style={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => navigate(`/perfil/${r.id}`)}>{r.name}</div>
                                <div style={{ color: '#666' }}>{r.faculdade || r.curso || r.estado}</div>
                            </div>
                            <button onClick={() => navigate(`/perfil/${r.id}`)}>Ver perfil</button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default ProfileSearch;
