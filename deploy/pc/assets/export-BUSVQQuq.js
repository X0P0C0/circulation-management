function d(o,s,l){const r="\uFEFF"+[s.join(","),...l.map(a=>a.map(c=>{const e=c==null?"":String(c);return e.includes(",")||e.includes('"')||e.includes(`
`)?'"'+e.replace(/"/g,'""')+'"':e}).join(","))].join(`
`),i=new Blob([r],{type:"text/csv;charset=utf-8;"}),t=URL.createObjectURL(i),n=document.createElement("a");n.href=t,n.download=o+"_"+new Date().toISOString().slice(0,10)+".csv",n.click(),URL.revokeObjectURL(t)}export{d as e};
