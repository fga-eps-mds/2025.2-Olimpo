import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';

function ProfilePage() {
    const { id } = useParams();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function load() {
            setLoading(true);
            try {
                const resp = await fetch(`http://localhost:8080/user/${id}`);
                if (!resp.ok) throw new Error('Perfil não encontrado');
                const data = await resp.json();
                setProfile(data);
            } catch (err) {
                console.error(err);
                setProfile(null);
            } finally {
                setLoading(false);
            }
        }
        load();
    }, [id]);

    if (loading) return <div style={{ padding: 20 }}>Carregando...</div>;
    if (!profile) return <div style={{ padding: 20 }}>Perfil não encontrado. <Link to="/perfil/search">Voltar à busca</Link></div>;

    return (
        <div style={{ padding: 20 }}>
            <Link to="/perfil/search">← Voltar</Link>
            <div style={{ display: 'flex', gap: 20, marginTop: 12 }}>
                <img src={profile.pfp || '/public/avatar.png'} alt="pfp" style={{ width: 128, height: 128, borderRadius: '50%' }} />
                <div>
                    <h2>{profile.name}</h2>
                    <p style={{ color: '#666' }}>{profile.role} • {profile.faculdade || ''} {profile.curso ? `• ${profile.curso}` : ''}</p>
                    <h4>Biografia</h4>
                    <p>{profile.bio || 'Sem biografia pública.'}</p>
                    <div style={{ marginTop: 12, color: '#444' }}>
                        <div><strong>Estado:</strong> {profile.estado || '-'}</div>
                        <div><strong>Semestre:</strong> {profile.semestre || '-'}</div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;
