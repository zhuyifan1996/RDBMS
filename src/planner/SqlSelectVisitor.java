package planner;

import java.util.HashMap;
import java.util.HashSet;

import operators.logical.DuplicateEliminateOperator;
import operators.logical.LogicalOperator;
import operators.logical.ProjectionOperator;
import operators.logical.ScanOperator;
import operators.logical.SortOperator;
import operators.physical.PhysicalOperator;
import main.Logger;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

public class SqlSelectVisitor extends Planner implements SelectVisitor {
		
	private PhysicalOperator op;
	
	public SqlSelectVisitor() {
		super();
	}

	@Override
	public PhysicalOperator getOperator(){
		return this.op;
	}
	
	@Override
	public void visit(PlainSelect arg0) {
		try {
			Logger.println("Plain select statement "+arg0);
			
			LogicalOperator logicalOp =  null;
			
			HashMap<String, operators.logical.ScanOperator> scanners = new HashMap<String, operators.logical.ScanOperator>();
			HashMap<Integer, operators.logical.ScanOperator> bucketToScanner = new HashMap<Integer, operators.logical.ScanOperator>();
			HashMap<String, Integer> tblNameToBuckets = new HashMap<String, Integer>();
			HashSet<String> baseTableNames = new HashSet<String>();
			
			
			// first implement the basic evaluation with only one table
			SqlFromItemVisitor frVisitor = new SqlFromItemVisitor();
			arg0.getFromItem().accept(frVisitor);
			
			int bucketIndex = 0;
			ScanOperator defaultTableScanner = frVisitor.getOperator();
			Table defaultTable = frVisitor.getTable();
			baseTableNames.add(defaultTable.getName());
			
			// double check that we got the default one, other wise will be invalid SQL
			if (defaultTableScanner == null){
				throw new Exception("Unable to get scanner while passing the sql query.");
			}
			
			if (frVisitor.getTable().getAlias() != null){
				ScanOperator op = frVisitor.getOperator(); 
				op.setAlias(frVisitor.getTable().getAlias());
				// also set up the pointer from alias -> the same ScanOperator
				scanners.put(frVisitor.getTable().getAlias(), op);
				bucketToScanner.put(bucketIndex, op);
				tblNameToBuckets.put(frVisitor.getTable().getAlias(), bucketIndex);
			}
			scanners.put(frVisitor.getTable().getName(), frVisitor.getOperator());
			tblNameToBuckets.put(frVisitor.getTable().getName(), bucketIndex);
			bucketIndex++;
			
			// Find all other tables in join.
			Logger.println("Joins:"+arg0.getJoins());
			if (arg0.getJoins() != null){				
				for (Object obj : arg0.getJoins()){
					Join joinObj = ((Join) obj);
					
					// Logger.println("Join:"+joinObj);					
					SqlFromItemVisitor fromItemVisitorForJoin = new SqlFromItemVisitor();
					joinObj.getRightItem().accept(fromItemVisitorForJoin);
					
					Table tbl = fromItemVisitorForJoin.getTable();
					baseTableNames.add(tbl.getName());
					ScanOperator op = fromItemVisitorForJoin.getOperator();
					bucketToScanner.put(bucketIndex, op);						

					// won't add it unless the base table exists
					if (!scanners.containsKey(tbl.getName())){
						scanners.put(tbl.getName(), op);
						tblNameToBuckets.put(tbl.getName(), bucketIndex);
					}
					
					if (tbl.getAlias() != null){
						op.setAlias(tbl.getAlias());
						// also set up the pointer from alias -> the same ScanOperator
						scanners.put(tbl.getAlias(), op);
						tblNameToBuckets.put(tbl.getAlias(), bucketIndex);
					}
					
					bucketIndex++;
				}	
			}
			
			Logger.println("Where Clause:" + arg0.getWhere());
			if (arg0.getWhere() != null){
				SqlWhereExpressionVisitor v = new SqlWhereExpressionVisitor(scanners, tblNameToBuckets, baseTableNames);
				arg0.getWhere().accept(v);
				logicalOp = v.getOperator();
			}else{
				logicalOp = frVisitor.getOperator();
				if (bucketIndex > 1){
					// Need to create the simplifed left-leaned join trees
					for (int i = 1; i < bucketIndex; i++){
						LogicalOperator op = bucketToScanner.get(i);
						logicalOp = new operators.logical.JoinOperator(logicalOp, op);
					}
				}
			}

			Logger.println("Select Items:" + arg0.getSelectItems());
			SqlSelectItemVisitor siVisitor = new SqlSelectItemVisitor(logicalOp, defaultTable, scanners);
			for (Object obj : arg0.getSelectItems().toArray()){
				SelectItem slItem = (SelectItem) obj;
				slItem.accept(siVisitor);
			}
			
			if (siVisitor.getOperator()!=null){
				ProjectionOperator op = siVisitor.getOperator(); 
				op.setChild(logicalOp);
				logicalOp = op;
			}
						
			//Handle OrderBy
			SqlOrderByVisitor oByVisitor = new SqlOrderByVisitor(logicalOp, baseTableNames);
			if( arg0.getOrderByElements() != null){
			for( Object obj : arg0.getOrderByElements()){
				//called for every column
				Logger.println("ORDER BY: "+obj);
				OrderByElement obe = (OrderByElement) obj;
				obe.accept( oByVisitor );
			}
			
			//Please make this the last statement (or second to last for distinct) so that order by is efficient
			SortOperator op = oByVisitor.getOperator();
			if( op != null )
				logicalOp = op;
			}
			
			if( arg0.getDistinct() != null){
				if( !(logicalOp instanceof SortOperator)){
					logicalOp = new SortOperator(logicalOp, null);
				}
				DuplicateEliminateOperator deo = new DuplicateEliminateOperator(logicalOp);
				logicalOp = deo;
			}
			
			Logger.println("Logical Tree:"+logicalOp);
			
			// Now plan the physical operator
			PhysicalPlanBuilder builder = new PhysicalPlanBuilder();
			logicalOp.accept(builder);
			this.op = builder.getPhysicalPlanResult();
			
			Logger.println("Physical Tree:"+this.op);
			Logger.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void visit(Union arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

}
