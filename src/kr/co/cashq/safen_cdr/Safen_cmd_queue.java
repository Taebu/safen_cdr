package kr.co.cashq.safen_cdr;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


//import com.nostech.safen.SafeNo;

/**

 * safen_cmd_queue 테이블 관련 객체
 * @author mtb
 * 2017-03-10 (금) 13:36:49 
 *  리뷰 포인트 90일 60일로 변경
 *  
 *  2017-08-18 (금) 16:10:00
 *   세일 포인트 추가
 *   동시 적립 가능하도록 변경 
 */
public class Safen_cmd_queue {
	
	/**
	 * safen_cmd_queue 테이블의 데이터를 처리하기 위한 주요한 처리를 수행한다.
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();
		/* 포인트 이벤트 정보 */
		Map<String, String> event_info = new HashMap<String, String>();
		
		/* 콜로그 정보 */
		Map<String, String> call_log = new HashMap<String, String>();
		
		String ev_st_dt="";
		String ev_ed_dt="";
		String daily_st_dt="";
		String daily_ed_dt="";
		String review_ed_dt="";
		String mb_hp="";
		String eventcode="";
		String cash="";
		String ed_type="";
		String biz_code="";
		String call_hangup_dt="";
		String mb_id="";
		String certi_code="";
		String st_dt="";
		String ed_dt="";
		String moddate="1970-01-01 12:00:00";
		String accdate="1970-01-01 12:00:00";
		String str_hangup_time="";
		String tel="";
		String pt_stat="";

		int eventcnt = 0;
		int daycnt = 0;
		int reviewdaycnt = 0;
		int downdaycnt = 0;
		int saledaycnt = 0;
		int calldaycnt = 0;
		int pt_day_cnt = 0;
		int pt_event_cnt = 0;
		int usereventindex = 0;
		int user_event_dt_index = 0;
		int tcl_seq = 0;
		int service_sec=0;
		int hangup_time=0;

		boolean is_hp = false;
		boolean is_freedailypt = false;
		boolean is_freeuserpt = false;
		boolean is_fivept = false;
		boolean is_callpt = false;
		boolean is_reviewpt = false;
		boolean is_downpt = false;
		boolean is_salept = false;
		
		boolean is_realcode=false;
		boolean is_userpt=false;
		boolean chk_realcode=false;
		boolean is_answer=false;

		/* 상점 정보 */
		String[] store_info			= new String[5];
		/* 포인트 이벤트 정보 */
		String[] point_event_info	= new String[7];
		/* 유저 이벤트 정보 */
		String[] user_event_info	= new String[3];
		
		String status_cd="";
		String conn_sdt="";
		String conn_edt="";
		String service_sdt="";
		String safen="";
		String safen_in="";
		String safen_out="";
		String calllog_rec_file="";

		String store_name = "";
		String pre_pay="";
		String store_seq="";
		String type="";
		String str_tcl_seq="";

		if (con != null) {
			MyDataObject dao = new MyDataObject();
			MyDataObject dao2 = new MyDataObject();
			MyDataObject dao3 = new MyDataObject();
			MyDataObject dao4 = new MyDataObject();
			MyDataObject dao5 = new MyDataObject();

			StringBuilder sb = new StringBuilder();
			StringBuilder sb_log = new StringBuilder();

			//sb.append("select exists(select 1 from safen_cdr) a");
			//sb.append("select * from safen_cdr limit 1");
			//sb.append("select * from safen_cdr where seq<192 limit 1;");
			//sb.append("select * from safen_cdr where seq<221 limit 1;");
			//sb.append("select * from safen_cdr limit 1;");
			sb.append("select * from sktl.safen_cdr order by seq;");
			
			try {
				dao.openPstmt(sb.toString());

				dao.setRs(dao.pstmt().executeQuery());

				while(dao.rs().next()) {
					
					SAFEN_CDR.heart_beat = 1;
					Boolean chk_seq=dao.rs().getInt("seq")>0;

					if (chk_seq) {
						StringBuilder sb2 = new StringBuilder();
						StringBuilder sb5 = new StringBuilder();
						String hist_table = DBConn.isExistTableYYYYMM();
						int resultCnt2 = 0;

						/*	String status_cd 상태코드, */
						status_cd=chkValue(dao.rs().getString("status_cd"));
						call_log.put("status_cd", chkValue(dao.rs().getString("status_cd")));
						
						/*	String conn_sdt 시작시간,  */
						conn_sdt=chkValue(dao.rs().getString("conn_sdt"));
						call_log.put("conn_sdt", chkValue(dao.rs().getString("conn_sdt")));
						
						/*	String conn_edt 종료시간, */
						conn_edt=chkValue(dao.rs().getString("conn_edt"));
						
						/*	String service_sdt, 반응시간 */
						service_sdt=chkValue(dao.rs().getString("service_sdt"));
						
						/*	String safen 안심번호, 050 */
						safen=chkValue(dao.rs().getString("safen"));
						tel=safen;

						/*	String safen_in 상점번호, */
						safen_in=chkValue(dao.rs().getString("safen_in"));
						
						/*	String calllog_rec_file	콜로그 녹음파일( */
						safen_out=chkValue(dao.rs().getString("safen_out"));
						
						/*	String status_cd 상태코드, */
						calllog_rec_file=chkValue(dao.rs().getString("calllog_rec_file"));
						
						/*	String status_cd 상태코드, */
						service_sec=dao.rs().getInt("service_sec");
						
						/*	String status_cd 상태코드, */
						call_hangup_dt=chkValue(dao.rs().getString("service_sdt"));
						
						/*	String status_cd 상태코드, */
						st_dt=chkValue(dao.rs().getString("conn_sdt"));
						
						/*	String status_cd 상태코드, */
						ed_dt=chkValue(dao.rs().getString("conn_edt"));
						
						st_dt=chgDatetime(st_dt);
						ed_dt=chgDatetime(ed_dt);
						mb_hp=safen_out;
						
						is_answer = status_cd.equals("1");
						/* cashq.TB_CALL_LOG에 세팅합니다. */
						tcl_seq = set_TB_CALL_LOG(status_cd, conn_sdt,conn_edt, service_sdt, safen,safen_in,safen_out,calllog_rec_file);
						
						tcl_seq = set_TB_CALL_LOG(call_log);
						
						str_tcl_seq=Integer.toString(tcl_seq);
						str_hangup_time=Integer.toString(service_sec);

						/* cashq.store.callcnt 갱신*/
						update_stcall(safen);
						
						/* 4-2. Store Info 조회 */
						//store_info=getStoreInfo(safen_in);
						store_info=getStoreInfo(safen);
						/**
						* store_info[0] = store_name, 상점이름 
						* store_info[1] = pre_pay, 골드,실버,캐시큐,일반,PRQ 
						* store_info[2] = biz_code, 비즈코드 
						* store_info[3] = store_seq, 상점고유번호 
						* store_info[4] = type, 상점타입(예, 치킨,피자 등등의 코드) 
						*/
						store_name=store_info[0];
						pre_pay=store_info[1];
						biz_code=store_info[2];
						store_seq=store_info[3];
						type=store_info[4];

						/* 4-3. Event Info 조회*/
						if(biz_code!=null||biz_code!="")
						{
						point_event_info=getEventCodeInfo(biz_code);
						event_info=getEventCodeInfoMap(biz_code);
						/**
						* point_event_info[0] = ev_st_dt, 이벤트 시작일
						* point_event_info[1] = ev_ed_dt, 이벤트 종료일
						* point_event_info[2] = eventcode, 이벤트 코드 
						* point_event_info[3] = cash, 캐시
						* point_event_info[4] = pt_day_cnt, 일정립제한 갯수
						* point_event_info[5] = pt_event_cnt, 이벤트코드 정립 제한 갯수
						* point_event_info[6] = ed_type, 이벤트코드 타입
						*/
						ev_st_dt=event_info.get("ev_st_dt");
						ev_ed_dt=event_info.get("ev_ed_dt");								
						eventcode=event_info.get("eventcode")!=null?event_info.get("eventcode"):"";
						
						cash=point_event_info[3]!=null?point_event_info[3]:"";
						pt_day_cnt = point_event_info[4]!=null?Integer.parseInt(point_event_info[4]):0;
						pt_event_cnt = point_event_info[5]!=null?Integer.parseInt(point_event_info[5]):0;
						ed_type=point_event_info[6];
						

						
						if(eventcode.length()>3&&biz_code.length()>3){
							chk_realcode=is_realcode(eventcode,biz_code);
						}

						/** 
						* 4-4. 이벤트 갯수 카운트&amp;
						* 4-5-1. call 하루 포인트 한번 포인트 여부
						* 4-5-2. reviewpt 하루 포인트 한번 포인트 여부
						* 4-5-2. 하루 포인트 한번 포인트 여부
						* 4-6. 핸드폰 번호 여부
						* 4-7. 포인트 재적립 시간 여부 단위
						*      point_event_dt, pt_event_cnt
						* 4-8. NEW freedailypt 여부 조회
						* 4-9. NEW 오늘을 기준으로 60일 코드를 가져온다.
						* 4-10. NEW freeuserpt 여부 조회
						* 4-11. NEW usereventcnt
						* 4-12. NEW
						* 4-13. NEW pt_stat
						*/


						
						/* 4-5 */
						//String[] callArray = new String[] {"reviewpt","downpt"};
						String[] callArray = new String[] {"fivept","freedailypt","freeuserpt","freept"};
						String[] reviewArray = new String[] {"reviewpt","downpt"};
						
						/* 4-6 */
						is_hp = is_hp(safen_out);

						/* 4-8 */
						is_freedailypt = is_freedailypt(ed_type);
						
						/* 4-10 */
						is_freeuserpt = is_freeuserpt(ed_type);

						/* 4-10-1 */
						is_fivept=is_fivept(ed_type);
						
						
						/* 4-10-2 */
						is_reviewpt=is_reviewpt(ed_type);
						/* 4-10-3 */
						is_downpt=is_downpt(ed_type);
						/* 4-10-4 */
						is_callpt=Arrays.asList(callArray).contains(ed_type);

						/* 4-10-5 */
						is_salept=is_salept(ed_type);
						
						
						if(is_callpt){
						//calldaycnt = get_daycnt(mb_hp,"callpt");
						calldaycnt = get_checkpoint("callpt",mb_hp);
						}else if(is_reviewpt){
						reviewdaycnt = get_checkpoint("reviewpt",mb_hp);
						}else if(is_downpt){
						downdaycnt =  get_checkpoint("downpt",mb_hp);
						}else if(is_salept){
						saledaycnt =  get_checkpoint("salept",mb_hp);
						}

						
						
						daily_st_dt=Utils.getyyyymmdd();
						daily_ed_dt=Utils.add60day();
						//review_ed_dt=Utils.add90day();
						review_ed_dt=Utils.add60day();
						
						/* 4-11 */
						if(is_freeuserpt){
							usereventindex = get_user_event_index(mb_hp, biz_code);
						}
						 
						/* 4-12 */
						if(is_freeuserpt&&usereventindex==0&&is_hp&&is_answer){
							/* user_event 생성하기 */
							daily_st_dt=Utils.getyyyymmdd();
							daily_ed_dt=Utils.add60day();
							eventcode=biz_code+"_1";
							user_event_dt_index = set_user_event_dt(biz_code, mb_hp, daily_st_dt,daily_ed_dt,eventcode);
						}else if(is_freeuserpt&&usereventindex>0&&is_hp&&is_answer){
							/* user_event 조회하기 */
							user_event_info = get_userevent(biz_code, mb_hp);
							if(is_datepoint(user_event_info[0],user_event_info[1])){
								daily_st_dt=user_event_info[0];
								daily_ed_dt=user_event_info[1];
								eventcode=user_event_info[2];
							}else{
								daily_st_dt=Utils.getyyyymmdd();
								daily_ed_dt=Utils.add60day();
								eventcode=chg_userevent(user_event_info[2]);
								user_event_dt_index = set_user_event_dt(biz_code, mb_hp, daily_st_dt,daily_ed_dt,eventcode);
							
							}
						}
						}/* if(biz_code!=null||biz_code!=""){ ... } */

						/* 4-4 */
						eventcnt = get_eventcnt(mb_hp,eventcode,ed_type);
						/* 4-13 */
						pt_stat=chk_pt5(ed_type);

						/* fivept, freept 6. 적립조건*/
						if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_fivept
							&&is_answer
							&&chk_realcode
							&&is_callpt
						){
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, ev_st_dt, 
								ev_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
						}else if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_answer
							&&is_freedailypt
							&&chk_realcode
							&&is_callpt
						){
							/* freedailypt 7. 적립조건*/
							daily_st_dt=Utils.getyyyymmdd();
							daily_ed_dt=Utils.add60day();
							

							/* 7-1 */
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, daily_st_dt,
								daily_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
						}else if(is_point(pre_pay)
							&&service_sec>9
							&&is_datepoint(ev_st_dt,ev_ed_dt)
							&&calldaycnt==0
							&&eventcnt<pt_event_cnt
							&&is_hp
							&&is_answer
							&&is_freeuserpt
							&&chk_realcode
							&&is_callpt
						){
							/* freeuserpt 8. 적립조건*/
							/* 8-1 */
							set_0507_point(
								mb_hp,store_name, str_hangup_time, 
								biz_code, call_hangup_dt, daily_st_dt, 
								daily_ed_dt, eventcode, mb_id, 
								certi_code, st_dt, ed_dt, 
								store_seq, str_tcl_seq, moddate, 
								accdate,ed_type,type,
								tel,pre_pay,pt_stat);
							
							set_checkpoint("callpt",mb_hp);
/*
무제한요청
&&eventcnt<pt_event_cnt 삭제
2018-04-06 (금) 16:44:16  https://github.com/Taebu/cashq/issues/283 */
						}else if(is_point(pre_pay)
								&&service_sec>9
								&&is_datepoint(ev_st_dt,ev_ed_dt)
								&&reviewdaycnt==0
								&&is_hp
								&&is_answer
								&&is_reviewpt
								&&chk_realcode
							){
								/* reviewpt 9. 적립조건*/
								/* 9-1 */
								set_0507_point(
									mb_hp,store_name, str_hangup_time, 
									biz_code, call_hangup_dt, daily_st_dt, 
									review_ed_dt, eventcode, mb_id, 
									certi_code, st_dt, ed_dt, 
									store_seq, str_tcl_seq, moddate, 
									accdate,ed_type,type,
									tel,pre_pay,pt_stat);
								
								set_checkpoint("reviewpt",mb_hp);
							}else if(is_point(pre_pay)
									&&service_sec>9
									&&is_datepoint(ev_st_dt,ev_ed_dt)
									&&downdaycnt==0
									&&eventcnt<pt_event_cnt
									&&is_hp
									&&is_answer
									&&is_downpt
									&&chk_realcode
								){
									/* downpt 10. 적립조건*/
									/* 10-1 */
									set_0507_point(
										mb_hp,store_name, str_hangup_time, 
										biz_code, call_hangup_dt, daily_st_dt, 
										daily_ed_dt, eventcode, mb_id, 
										certi_code, st_dt, ed_dt, 
										store_seq, str_tcl_seq, moddate, 
										accdate,ed_type,type,
										tel,pre_pay,pt_stat);
									
									set_checkpoint("downpt",mb_hp);
								}else if(is_point(pre_pay)
										&&is_datepoint(ev_st_dt,ev_ed_dt)
										&&saledaycnt<pt_day_cnt
										&&eventcnt<pt_event_cnt
										&&is_hp
										&&is_answer
										&&is_salept
										&&chk_realcode
								){
									/* salept 11. 적립조건*/
									/* 11-1 */
									set_0507_point(
										mb_hp,store_name, str_hangup_time, 
										biz_code, call_hangup_dt, daily_st_dt, 
										daily_ed_dt, eventcode, mb_id, 
										certi_code, st_dt, ed_dt, 
										store_seq, str_tcl_seq, moddate, 
										accdate,ed_type,type,
										tel,pre_pay,pt_stat);
									
									set_checkpoint("salept",mb_hp);
								}
						


						sb2.append("insert into sktl.");
						sb2.append(hist_table);
						/* 처리가 진행중인것은 포함하지 않는다. */
						sb2.append(" select * from sktl.safen_cdr where seq=?");
						
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao2.openPstmt(sb2.toString());
						dao2.pstmt().setInt(1, dao.rs().getInt("seq"));

 						resultCnt2 = dao2.pstmt().executeUpdate();
						if(resultCnt2!=1) {
							Utils.getLogger().warning(dao2.getWarning(resultCnt2,1));
							DBConn.latest_warning = "ErrPOS027";
						}

						// region 3 start --->
						StringBuilder sb3 = new StringBuilder();

						/* 처리가 진행중인것은 지우지 않는다. */
						sb3.append("delete from sktl.safen_cdr where seq=?");
				
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao3.openPstmt(sb3.toString());
						dao3.pstmt().setInt(1, dao.rs().getInt("seq"));

						int resultCnt3 = dao3.pstmt().executeUpdate();
						if(resultCnt3!=1) {
							Utils.getLogger().warning(dao3.getWarning(resultCnt3,1));
							DBConn.latest_warning = "ErrPOS028";
						}
						// region 3 end <---

						// region 4 start --->
						/*
						StringBuilder sb4 = new StringBuilder();
						
						sb4.append("select * from safen_cdr limit 1");
						dao4.openPstmt(sb4.toString());

						dao4.setRs(dao4.pstmt().executeQuery());

						if (dao4.rs().next()) {
							int seq = dao4.rs().getInt("seq");
							String safen = dao4.rs().getString("safen");
							String safen_in = dao4.rs().getString("safen_in");
							doMapping(seq, safen, safen_in);
						}
						*/
						// region 4 end <---
					} else {
						//Utils.getLogger().info("chk_seq false log");
						if (!"".equals(Env.confirmSafen)) {
							// cmq_queue에는 없는 경우라면
							//SafeNo safeNo = new SafeNo();
							String retCode = "";
							try {
								//retCode = safeNo.SafeNoAsk(Env.getInstance().CORP_CODE,Env.confirmSafen);
							} catch (Exception e) {
								Utils.getLogger().warning(e.getMessage());
								Utils.getLogger().warning(Utils.stack(e));
								DBConn.latest_warning = "ErrPOS029";
							}

							if (-1 < retCode.indexOf(Env.confirmSafen_in)) {
								/* retCode = "01040421182,01040421182" 와 같은 형태로 리턴되는 식임 */
								Utils.getLogger().info(
										"OK 착신연결성공" + Env.confirmSafen + "->"
												+ Env.confirmSafen_in);
							} else {// 취소된 경우 recCode = "E401"이 리턴됨
								if (Env.NULL_TEL_NUMBER
										.equals(Env.confirmSafen_in)
										&& "E401".equals(retCode)) {
									Utils.getLogger().info(
											"OK 착신취소성공" + Env.confirmSafen
													+ ", retCode:[" + retCode
													+ "]");
								} else {
									Utils.getLogger().warning(
											"Error! " + Env.confirmSafen + "->"
													+ Env.confirmSafen_in
													+ "? retCode:[" + retCode
													+ "]");
									DBConn.latest_warning = "ErrPOS030";
								}
							}

							Env.confirmSafen = "";
						}
					}
				}				
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS031";
				e.printStackTrace();
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS032";
			}finally {
				dao.closePstmt();
				dao2.closePstmt();
				dao3.closePstmt();
				dao4.closePstmt();
				dao5.closePstmt();
			}
			
			//콜로그 마스터 정보의 레코드 1개를 갱신을 시도한다.
			//Safen_master.doWark2();
		}
	}

	
	/**
	 * TB_CALL_LOG에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_TB_CALL_LOG(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`TB_CALL_LOG` SET ");
		sb.append("SVC_ID='81',");
		sb.append("START_DT=?,");
		sb.append("END_DT=?,");
		sb.append("CALLED_HANGUP_DT=?,");
		sb.append("VIRTUAL_NUM=?,");
		sb.append("CALLED_NUM=?,");
		sb.append("CALLER_NUM=?,");
		sb.append("userfield=?,");
		sb.append("REASON_CD=?");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			if ("1".equals(status_cd)) {
			/* GCM LOG 발생*/
			set_stgcm(safen, safen_in);

			/* 통화성공 */
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, service_sdt);
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}else{
			/* 통화실패*/
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, "1970-01-01 09:00:00");
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}

			dao.pstmt().executeUpdate();


			sb2.append("select LAST_INSERT_ID() last_id;");
			dao2.openPstmt(sb2.toString());
			dao2.setRs(dao2.pstmt().executeQuery());
			
			if (dao2.rs().next()) {
				last_id = dao2.rs().getInt("last_id");
			}
			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
			dao2.closePstmt();
		}

		return last_id;
	}	
	/**
	 * TB_CALL_LOG에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_TB_CALL_LOG(Map<String,String> call_log) 
	{
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`TB_CALL_LOG` SET ");
		sb.append("SVC_ID='81',");
		sb.append("START_DT=?,");
		sb.append("END_DT=?,");
		sb.append("CALLED_HANGUP_DT=?,");
		sb.append("VIRTUAL_NUM=?,");
		sb.append("CALLED_NUM=?,");
		sb.append("CALLER_NUM=?,");
		sb.append("userfield=?,");
		sb.append("REASON_CD=?");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			if ("1".equals(call_log.get("status_cd"))) {
			/* GCM LOG 발생*/
			set_stgcm(call_log.get("safen"), call_log.get("safen_in"));

			/* 통화성공 */
			dao.pstmt().setString(1, call_log.get("conn_sdt"));
			dao.pstmt().setString(2, call_log.get("conn_edt"));
			dao.pstmt().setString(3, call_log.get("service_sdt"));
			dao.pstmt().setString(4, call_log.get("safen"));
			dao.pstmt().setString(5, call_log.get("safen_in"));
			dao.pstmt().setString(6, call_log.get("safen_out"));
			
			dao.pstmt().setString(7, call_log.get("calllog_rec_file"));
			dao.pstmt().setString(8, call_log.get("status_cd"));
			}else{
			/* 통화실패*/
			dao.pstmt().setString(1, call_log.get("conn_sdt"));
			dao.pstmt().setString(2, call_log.get("conn_edt"));
			dao.pstmt().setString(3, "1970-01-01 09:00:00");
			dao.pstmt().setString(4, call_log.get("safen"));
			dao.pstmt().setString(5, call_log.get("safen_in"));
			dao.pstmt().setString(6, call_log.get("safen_out"));
			
			dao.pstmt().setString(7, call_log.get("calllog_rec_file"));
			dao.pstmt().setString(8, call_log.get("status_cd"));				
			}

			dao.pstmt().executeUpdate();


			sb2.append("select LAST_INSERT_ID() last_id;");
			dao2.openPstmt(sb2.toString());
			dao2.setRs(dao2.pstmt().executeQuery());
			
			if (dao2.rs().next()) {
				last_id = dao2.rs().getInt("last_id");
			}
			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
			dao2.closePstmt();
		}

		return last_id;
	}


	/**
	 * 0507_point에 추가한다.
	* @param  mb_hp, 
	* @param  store_name, 
	* @param  hangup_time,
	* @param  biz_code,
	* @param  call_hangup_dt,
	* @param  pev_st_dt,
	* @param  pev_ed_dt,
	* @param  eventcode,
	* @param  mb_id,
	* @param  certi_code,
	* @param  st_dt,
	* @param  ed_dt,
	* @param  store_seq,
	* @param  tcl_seq,
	* @param  moddate,
	* @param  accdate,
	* @param  ed_type,
	* @param  type
	* @param  tel
	* @param  pre_pay
	* @param  pt_stat
	 * @return void
	 */
	public static void set_0507_point(
		String mb_hp, 
		String store_name, 
		String hangup_time,
		String biz_code,
		String call_hangup_dt,
		String pev_st_dt,
		String pev_ed_dt,
		String eventcode,
		String mb_id,
		String certi_code,
		String st_dt,
		String ed_dt,
		String store_seq,
		String tcl_seq,
		String moddate,
		String accdate,
		String ed_type,
		String type,
		String tel,
		String pre_pay,
		String pt_stat
	) 
	{

		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`0507_point` SET ");
		sb.append("mb_hp=?,");
		sb.append("store_name=?,");
		sb.append("point='2000',");
		sb.append("hangup_time=?,");
		sb.append("biz_code=?,");
		sb.append("call_hangup_dt=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("mb_id=?,");
		sb.append("certi_code=?,");
		sb.append("insdate=now(),");
		sb.append("st_dt=?,");
		sb.append("ed_dt=?,");
		sb.append("tcl_seq=?,");
		sb.append("store_seq=?,");
		sb.append("moddate=?,");
		sb.append("accdate=?, ");
		sb.append("ed_type=?, ");
		sb.append("type=?, ");
		sb.append("tel=?, ");
		sb.append("pre_pay=?, ");
		sb.append("pt_stat=? ");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, store_name);
			dao.pstmt().setString(3, hangup_time);
			dao.pstmt().setString(4, biz_code);
			dao.pstmt().setString(5, call_hangup_dt);
			dao.pstmt().setString(6, pev_st_dt);
			dao.pstmt().setString(7, pev_ed_dt);
			dao.pstmt().setString(8, eventcode);
			dao.pstmt().setString(9, mb_id);
			dao.pstmt().setString(10, certi_code);
			dao.pstmt().setString(11, st_dt);
			dao.pstmt().setString(12, ed_dt);
			dao.pstmt().setString(13, tcl_seq);
			dao.pstmt().setString(14, store_seq);
			dao.pstmt().setString(15, moddate);
			dao.pstmt().setString(16, accdate);
			dao.pstmt().setString(17, ed_type);
			dao.pstmt().setString(18, type);
			dao.pstmt().setString(19, tel);
			dao.pstmt().setString(20, pre_pay);
			dao.pstmt().setString(21, pt_stat);

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
	}




	/*************************************************
	* 0507_point에 추가한다.
	*  @param  Map<String, String>point_info, 
	*  @return void
	**************************************************/
	public static void set_0507_point(Map<String, String> point_info) 
	{
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`0507_point` SET ");
		sb.append("mb_hp=?,");
		sb.append("store_name=?,");
		sb.append("point='2000',");
		sb.append("hangup_time=?,");
		sb.append("biz_code=?,");
		sb.append("call_hangup_dt=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("mb_id=?,");
		sb.append("certi_code=?,");
		sb.append("insdate=now(),");
		sb.append("st_dt=?,");
		sb.append("ed_dt=?,");
		sb.append("tcl_seq=?,");
		sb.append("store_seq=?,");
		sb.append("moddate=?,");
		sb.append("accdate=?, ");
		sb.append("ed_type=?, ");
		sb.append("type=?, ");
		sb.append("tel=?, ");
		sb.append("pre_pay=?, ");
		sb.append("pt_stat=? ");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, point_info.get("mb_hp"));
			dao.pstmt().setString(2, point_info.get("store_name"));
			dao.pstmt().setString(3, point_info.get("hangup_time"));
			dao.pstmt().setString(4, point_info.get("biz_code"));
			dao.pstmt().setString(5, point_info.get("call_hangup_dt"));
			dao.pstmt().setString(6, point_info.get("ev_st_dt"));
			dao.pstmt().setString(7, point_info.get("ev_ed_dt"));
			dao.pstmt().setString(8, point_info.get("eventcode"));
			dao.pstmt().setString(9, point_info.get("mb_id"));
			dao.pstmt().setString(10, point_info.get("certi_code"));
			dao.pstmt().setString(11, point_info.get("st_dt"));
			dao.pstmt().setString(12, point_info.get("ed_dt"));
			dao.pstmt().setString(13, point_info.get("tcl_seq"));
			dao.pstmt().setString(14, point_info.get("store_seq"));
			dao.pstmt().setString(15, point_info.get("moddate"));
			dao.pstmt().setString(16, point_info.get("accdate"));
			dao.pstmt().setString(17, point_info.get("ed_type"));
			dao.pstmt().setString(18, point_info.get("type"));
			dao.pstmt().setString(19, point_info.get("tel"));
			dao.pstmt().setString(20, point_info.get("pre_pay"));
			dao.pstmt().setString(21, point_info.get("pt_stat"));

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
	}


	
	/**
	 * cdr 에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_cdr(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `asteriskcdrdb`.`cdr` SET ");
		sb.append("calldate=?,");
		sb.append("src=?,");
		sb.append("dst=?,");
		sb.append("duration=?,");
		sb.append("billsec=?,");
		sb.append("accountcode=?,");
		sb.append("uniqueid=?,");
		sb.append("userfield=?;");
		//Utils.getLogger().warning(sb.toString());


		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, dao.rs().getString("conn_sdt"));
			dao.pstmt().setString(2, dao.rs().getString("safen_in"));
			dao.pstmt().setString(3, dao.rs().getString("safen"));
			dao.pstmt().setString(4, dao.rs().getString("conn_sec"));
			dao.pstmt().setString(5, dao.rs().getString("service_sec"));
			dao.pstmt().setString(6, dao.rs().getString("safen_out"));
			dao.pstmt().setString(7, dao.rs().getString("unique_id"));
			dao.pstmt().setString(8, dao.rs().getString("rec_file_cd"));

			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		//return retVal;
		return last_id;
	}
	
	
	/**
	 * 상점 콜로그로 갱신한다.  retCode가 "0000"(성공)인경우에는 status_cd값을 "s"로 그렇지 않은 경우에는 "e"로 셋팅한 후 큐를
	 * 지우고 로그로 보낸다. 
	 * @param safen_in
	 * @param retCode
	 */
	private static void update_stcall(String safen) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`store` SET callcnt=callcnt+1 WHERE tel=?");

			// status_cd 컬럼을 "i"<진행중>상태로 바꾼다.
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen);

			int cnt = dao.pstmt().executeUpdate();

			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	 * 캐시큐 상점에서 안심번호에 따른 상점 정보를 리턴한다.
	 * @param safen
	 * @return
	 */
	private static String[] getStoreInfo(String safen) {
		String[] s = new String[5];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("select name,pre_pay,biz_code,seq,type from `cashq`.`store` where tel= ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("name");
				s[1] = dao.rs().getString("pre_pay");
				s[2] = dao.rs().getString("biz_code");
				s[3] = dao.rs().getString("seq");
				s[4] = dao.rs().getString("type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	/**
	 * 비즈코드에 따른 이벤트 코드 정보를 리턴한다.
	 * @param biz_code
	 * @return
	 */
	private static String[] getEventCodeInfo(String biz_code) {
		String[] s = new String[7];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		
		sb.append("SELECT ");
		sb.append("ev_st_dt,");
		sb.append("ev_ed_dt,");
		sb.append("eventcode,");
		sb.append("cash,");
		sb.append("pt_day_cnt,");
		sb.append("pt_event_cnt,");
		sb.append("ed_type ");
		sb.append("FROM `cashq`.`point_event_dt` ");
		sb.append("WHERE biz_code=? and used='1' ");
		sb.append("order by seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
				s[3] = dao.rs().getString("cash");
				s[4] = dao.rs().getString("pt_day_cnt");
				s[5] = dao.rs().getString("pt_event_cnt");
				s[6] = dao.rs().getString("ed_type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}
	
	
	/**
	 * @param appid
	 * @return
	 * 
	 */
	
	private static Map<String, String> getEventCodeInfoMap(String biz_code) {
		// TODO Auto-generated method stub
		Map<String, String> event_info=new HashMap<String, String>();
	
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("SELECT ");
		sb.append("ev_st_dt,");
		sb.append("ev_ed_dt,");
		sb.append("eventcode,");
		sb.append("cash,");
		sb.append("pt_day_cnt,");
		sb.append("pt_event_cnt,");
		sb.append("ed_type ");
		sb.append("FROM `cashq`.`point_event_dt` ");
		sb.append("WHERE biz_code=? and used='1' ");
		sb.append("order by seq desc limit 1;");
		
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.setRs (dao.pstmt().executeQuery());

			while(dao.rs().next()) 
			{
				event_info.put("ev_st_dt",dao.rs().getString("ev_st_dt"));
				event_info.put("ev_ed_dt",dao.rs().getString("ev_ed_dt"));
				event_info.put("eventcode",dao.rs().getString("eventcode"));
				event_info.put("cash",dao.rs().getString("cash"));
				event_info.put("pt_day_cnt",dao.rs().getString("pt_day_cnt"));
				event_info.put("pt_event_cnt",dao.rs().getString("pt_event_cnt"));
				event_info.put("ed_type",dao.rs().getString("ed_type"));			
			}			
		}catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}
		return event_info;
	}
	
	/**
	* is_realcode
	*/
	private static boolean is_realcode(String eventcode,String biz_code) {
		boolean is_code=false;

		String[] explode=eventcode.split("\\_");

		is_code=explode[0].equals(biz_code);
		return is_code;
	}

	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_eventcnt(String mb_hp, String eventcode,String ed_type){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`0507_point` ");
		sb.append("WHERE mb_hp=? ");
		sb.append("AND eventcode=? ");
		sb.append("AND ed_type=? ");
		sb.append("AND status in ('1','2','3','4');");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, mb_hp);
			dao.pstmt().setString(2, eventcode);
			dao.pstmt().setString(3, ed_type);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	/**
	* boolean is_hp
	* @param hp
	* @return boolean
	*/
	private static boolean is_hp(String hp){
		boolean retVal=false;
			if(hp.length()>=2){
				retVal=hp.substring(0,2).equals("01");
			}
		return retVal;
	}
	
	/**
	* boolean is_freedailypt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freedailypt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=11){
				retVal = ed_type.substring(0,11).equals("freedailypt");
			}
		}
		return retVal;
	}


	/**
	* boolean is_freeuserpt
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_freeuserpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=10){
				retVal = ed_type.substring(0,10).equals("freeuserpt");
			}
		}
		return retVal;
	}

	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_fivept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("fivept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_downpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=6){
				retVal = ed_type.substring(0,6).equals("downpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* boolean is_fivept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_reviewpt(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=8){
				retVal = ed_type.substring(0,8).equals("reviewpt");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}

	/**
	* boolean is_salept
	* @param ed_type
	* @return boolean
	*/
	private static boolean is_salept(String ed_type){
		boolean retVal=false;
		if(ed_type!=null){
			if(ed_type.length()>=5){
				retVal = ed_type.substring(0,6).equals("salept");
			}else{
				retVal = ed_type.equals("");
			}
		}
		return retVal;
	}
	/**
	* int get_user_event_index
	* @param mb_hp
	* @param biz_code
	* @return int
	*/
	private static int get_user_event_index(String mb_hp,String biz_code){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("and mb_hp=? ");
		sb.append("order by seq desc limit 1");
		
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	/**
	 * set_user_event_dt에 추가한다.
	 * @param String biz_code	콜로그 상태 코드
	 * @param String mb_hp	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in	링크된번호
	 * @param String safen_out	소비자 번호
	 * @param String calllog_rec_file	
	 * @return
	 */
	public static int set_user_event_dt(String biz_code, 
		String mb_hp, 
		String daily_st_dt,
		String daily_ed_dt,
		String eventcode) 
	{
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`user_event_dt` SET ");
		sb.append("biz_code=?,");
		sb.append("mb_hp=?,");
		sb.append("ev_st_dt=?,");
		sb.append("ev_ed_dt=?,");
		sb.append("eventcode=?,");
		sb.append("insdate=now()");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			dao.pstmt().setString(3, daily_st_dt);
			dao.pstmt().setString(4, daily_ed_dt);
			dao.pstmt().setString(5, eventcode);

			//dao.pstmt().executeQuery();
			dao.pstmt().executeUpdate();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		return last_id;
	}
	/**
	* get_userevent(biz_code, mb_hp)
	* @param biz_code
	* @param mb_hp
	* @return array
	*/
	private static String[] get_userevent(String biz_code, String mb_hp) {
		String[] s = new String[3];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT  ");
		sb.append("eventcode,");
		sb.append("ev_ed_dt,");
		sb.append("ev_st_dt ");
		sb.append("FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("AND mb_hp=? ");
		sb.append("ORDER BY seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			dao.pstmt().setString(2, mb_hp);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	/**
	* get_userevent(biz_code, mb_hp)
	* @param biz_code
	* @param mb_hp
	* @return array
	*/
	private static Map<String,String> get_userevent_map(Map<String, String> point_info ) {
		Map<String,String> user_event = new HashMap<String,String>();
		String[] s = new String[3];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT  ");
		sb.append("eventcode,");
		sb.append("ev_ed_dt,");
		sb.append("ev_st_dt ");
		sb.append("FROM `cashq`.`user_event_dt` ");
		sb.append("WHERE biz_code=? ");
		sb.append("AND mb_hp=? ");
		sb.append("ORDER BY seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, point_info.get("biz_code"));
			dao.pstmt().setString(2, point_info.get("mb_hp"));
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				 user_event.put("ev_st_dt",dao.rs().getString("ev_st_dt"));
				 user_event.put("ev_ed_dt",dao.rs().getString("ev_ed_dt"));
				 user_event.put("eventcode",dao.rs().getString("eventcode"));
				
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return user_event;
	}
	
	/**
	 * set_stgcm 아이디의 지역 정보를 갱신해서 넣는다.
	 * set_stgcm(safen, safen_in);
	 * @param safen
	 * @param safen_in
	 * @return void
	 */
	private static void set_stgcm(String safen,String safen_in) 
	{

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO cashq.st_gcm SET VIRTUAL_NUM=?,CALLED_NUM=?,insdate=now();");

			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen);
			dao.pstmt().setString(2, safen_in);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	* boolean is_point
	* @param pre_pay
	* @return boolean
	*/
	private static boolean is_point(String pre_pay){
		boolean retVal=false;
		
		if(pre_pay!=null){
			retVal = pre_pay.equals("gl")||pre_pay.equals("sl")||pre_pay.equals("on")||pre_pay.equals("br");
		}
		
		return retVal;
	}

	/**
	* boolean is_datepoint
	* @param ev_st_dt
	* @param ev_ed_dt
	* @return boolean
	*/
	private static boolean is_datepoint(String ev_st_dt,String ev_ed_dt){
		boolean is_date=false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{

		/* null check 하나라도 널이면 에러 */
		if(ev_st_dt==null||ev_ed_dt==null){

		}else{
			Date todayDate = new Date();
			
			Date historyDate = sdf.parse(ev_st_dt);
			Date futureDate = sdf.parse(ev_ed_dt);

			/* 기간 이내 */
			is_date=todayDate.after(historyDate)&&todayDate.before(futureDate);
			
			/* 이벤트 종료 시간과 같은 날 */
			if(sdf.format(todayDate).equals(sdf.format(futureDate))){
				is_date=true;
			}		
		}
		
		}catch(ParseException e){
		
		}
		
		return is_date;
	}


	// yyyy-MM-dd HH:mm:ss.0 을 yyyy-MM-dd HH:mm:ss날짜로 변경
	public static String chgDatetime(String str)
	{
		String retVal="";

		try{
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date historyDate = simpleDate.parse(str);
		retVal=simpleDate.format(historyDate);
		}catch(ParseException e){
		}
		return retVal;
	}


    /**
     * chkValue
	 *  데이터 유효성 null 체크에 대한 값을 "" 로 리턴한다.
     * @param str
     * @return String
     */
	public static String chkValue(String str)
	{
		String retVal="";

		try{
				retVal=str==null?"":str;
		}catch(NullPointerException e){
			
		}
		return retVal;
	}
	
	public static String chk_pt5(String str)
	{
		String retVal="pt5";
		String[] ed_type= new String[] {"freept","freedailypt","freeuserpt"};

		if(Arrays.asList(ed_type).contains(str)){
			retVal="free";
		}

			return retVal; 
	}

	private static String chg_userevent(String eventcode) {
		String retVal="";
		String[] explode=eventcode.split("\\_");
		int up_usercnt=Integer.parseInt(explode[1]);
		up_usercnt++;

		retVal=explode[0]+"_"+up_usercnt;
		return retVal;
	}
	
	


	/**
	 * set_checkpoint
	 * @param cp_type
	 * @param cp_hp
	 * @return
	 */
	public static int set_checkpoint(String cp_type,String cp_hp) 
	{
		int last_id = 0;
		long unixtime=0L;
		unixtime=System.currentTimeMillis() / 1000;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `cashq`.`check_point` SET ");
		sb.append("cp_date=?,");
		sb.append("cp_unixtime=?,");
		sb.append("cp_type=?,");
		sb.append("cp_hp=?");
		try {
			dao.openPstmt(sb.toString());

			//Utils.getLogger().warning(sb.toString());

			
			dao.pstmt().setString(1, Utils.getyyyymmdd());
			dao.pstmt().setLong(2, unixtime);
			dao.pstmt().setString(3, cp_type);
			dao.pstmt().setString(4, cp_hp);
			dao.pstmt().executeUpdate();


			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}

		return last_id;
	}

	
	/**
	* int get_eventcnt
	* @param mb_hp
	* @param eventcode
	* @return int
	*/
	private static int get_checkpoint(String cp_type, String cp_hp){
		int retVal = 0;
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT count(*) cnt FROM `cashq`.`check_point` ");
		sb.append("WHERE cp_hp=? ");
		sb.append("AND cp_type=? ");
		sb.append("AND cp_date=? ");
		

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1,	cp_hp);
			dao.pstmt().setString(2, cp_type);
			dao.pstmt().setString(3, Utils.getyyyymmdd());
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getInt("cnt");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}
}
