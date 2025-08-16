// Tab switching function
function openTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.style.display = 'none');
    document.getElementById(tabName).style.display = 'block';
    if (tabName === 'records') loadRecords();
}

// Dynamic maid name inputs based on numMaids
document.getElementById('numMaids').addEventListener('change', (e) => {
    const num = parseInt(e.target.value);
    const container = document.getElementById('maidNames');
    container.innerHTML = '';
    for (let i = 1; i <= num; i++) {
        const input = document.createElement('input');
        input.type = 'text';
        input.placeholder = `Nome da Camareira ${i}`;
        input.required = true;
        container.appendChild(input);
        container.appendChild(document.createElement('br'));
    }
});

// Form submit: Register new day
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const numMaids = document.getElementById('numMaids').value;
    const maidInputs = document.querySelectorAll('#maidNames input');
    const maidNames = Array.from(maidInputs).map(input => input.value);
    const excludedRooms = document.getElementById('excludedRooms').value;

    try {
        const response = await fetch('/api/cleaning/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ numMaids, maidNames, excludedRooms })
        });
        if (response.ok) {
            alert('Registro salvo com sucesso!');
            document.getElementById('registerForm').reset();
            document.getElementById('maidNames').innerHTML = '';
        } else {
            alert('Erro ao salvar.');
        }
    } catch (error) {
        console.error('Error:', error);
    }
});

// Load all records and display
async function loadRecords() {
    try {
        const response = await fetch('/api/cleaning/all');
        const records = await response.json();
        const list = document.getElementById('recordsList');
        list.innerHTML = '';
        records.forEach(record => {
            const div = document.createElement('div');
            div.innerHTML = `
                <h3>Dia: ${record.registrationTime}</h3>
                <p>Camareiras: ${record.maids.join(', ')}</p>
                <p>Quartos a Limpar: ${record.roomsToClean.join(', ')}</p>
                <p>Atribuições:</p>
                <ul>${record.assignments.map(a => `<li>${a}</li>`).join('')}</ul>
                <button onclick="editRecord(${record.id})">Editar</button>
                <button onclick="deleteRecord(${record.id})">Deletar</button>
                <button onclick="printRecord(${record.id})">Imprimir</button>
                <hr>
            `;
            list.appendChild(div);
        });
    } catch (error) {
        console.error('Error loading records:', error);
    }
}

// Edit record: Prompt for new values and update
async function editRecord(id) {
    const numMaids = prompt('Novo número de camareiras:');
    if (!numMaids) return;
    const maidNames = [];
    for (let i = 1; i <= numMaids; i++) {
        const name = prompt(`Nome da Camareira ${i}:`);
        if (name) maidNames.push(name);
    }
    const excludedRooms = prompt('Novos quartos excluídos (separados por vírgula):');
    try {
        const response = await fetch(`/api/cleaning/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ numMaids, maidNames, excludedRooms })
        });
        if (response.ok) {
            alert('Atualizado!');
            loadRecords();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// Delete record
async function deleteRecord(id) {
    if (confirm('Deletar este registro?')) {
        try {
            const response = await fetch(`/api/cleaning/${id}`, { method: 'DELETE' });
            if (response.ok) {
                alert('Deletado!');
                loadRecords();
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
}

// Print a single record: Generate nice table and print only the printArea
async function printRecord(id) {
    try {
        const response = await fetch(`/api/cleaning/${id}`);
        const record = await response.json();
        const printArea = document.getElementById('printArea');
        
        // Format date: Ex. "Dia 15/08/2025" (ajuste o formato conforme necessário)
        const date = new Date(record.registrationTime);
        const formattedDate = `Dia ${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`;
        
        // Group roomsToClean by floor
        const roomsByFloor = {};
        record.roomsToClean.forEach(room => {
            const floor = Math.floor(room / 100) + 'º Andar';
            if (!roomsByFloor[floor]) roomsByFloor[floor] = [];
            roomsByFloor[floor].push(room);
        });
        
        // Parse assignments and group by maid and floor
        const assignmentsByMaid = {};
        record.assignments.forEach(assignment => {
            const [maid, roomsStr] = assignment.split(': ');
            assignmentsByMaid[maid] = {};
            const rooms = roomsStr.split(', ').map(Number);
            rooms.forEach(room => {
                const floor = Math.floor(room / 100) + 'º Andar';
                if (!assignmentsByMaid[maid][floor]) assignmentsByMaid[maid][floor] = [];
                assignmentsByMaid[maid][floor].push(room);
            });
        });
        
        // Generate HTML similar to PDF
        let html = `<h2>${formattedDate}</h2>
                    <h3>Quartos a serem limpos</h3>`;
        
        // List all rooms to clean by floor
        for (const floor in roomsByFloor) {
            html += `<p>${floor}: ${roomsByFloor[floor].sort((a,b)=>a-b).join(', ')}</p>`;
        }
        
        // Table for assignments: Columns like PDF (Camareira, Andar, Nº do quarto)
        html += `<h3>Atribuições</h3>
                 <table>
                     <thead>
                         <tr>
                             <th>Camareira</th>
                             <th>Andar</th>
                             <th>Nº do quarto</th>
                         </tr>
                     </thead>
                     <tbody>`;
        
        // Add rows for each maid/floor combo
        Object.keys(assignmentsByMaid).sort().forEach(maid => {
            const floors = assignmentsByMaid[maid];
            Object.keys(floors).sort().forEach(floor => {
                html += `<tr>
                             <td>${maid}</td>
                             <td>${floor}</td>
                             <td>${floors[floor].sort((a,b)=>a-b).join(', ')}</td>
                         </tr>`;
            });
        });
        
        html += `</tbody></table>`;
        
        printArea.innerHTML = html;
        window.print();
        
        // Clear after print (use timeout to allow print dialog to appear)
        setTimeout(() => {
            printArea.innerHTML = '';
        }, 1000);
    } catch (error) {
        console.error('Error:', error);
    }
}

// Download all records as CSV
async function downloadAllCSV() {
    try {
        const response = await fetch('/api/cleaning/all');
        const records = await response.json();
        let csv = 'ID,Dia,Hora,Camareiras,Quartos a Limpar,Atribuições\n';
        records.forEach(record => {
            csv += `${record.id},${record.registrationTime.split('T')[0]},${record.registrationTime.split('T')[1]},${record.maids.join(';')},${record.roomsToClean.join(';')},${record.assignments.join(';')}\n`;
        });
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'todos_registros.csv';
        a.click();
    } catch (error) {
        console.error('Error:', error);
    }
}

// Initial load: Open first tab
openTab('register');